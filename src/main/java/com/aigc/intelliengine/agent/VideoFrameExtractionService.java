package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.asset.FileStorageService;
import com.aigc.intelliengine.common.exception.BusinessException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VideoFrameExtractionService {

    private final VideoFrameMapper videoFrameMapper;
    private final FileStorageService fileStorageService;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public VideoFrameExtractionService(VideoFrameMapper videoFrameMapper,
                                        FileStorageService fileStorageService,
                                        MinioClient minioClient) {
        this.videoFrameMapper = videoFrameMapper;
        this.fileStorageService = fileStorageService;
        this.minioClient = minioClient;
    }

    @Value("${ffmpeg.path:}")
    private String configuredFfmpegPath;

    private static final int MAX_FRAMES = 20;
    private static final int THUMBNAIL_WIDTH = 320;

    private volatile String ffmpegPath;

    /**
     * Extract keyframes from a video stored in MinIO.
     * Returns the number of frames extracted.
     */
    public int extractFrames(Long aiVideoId, String objectName, int fps, BigDecimal duration) {
        // Try FFmpeg first
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("intelliengine-frames-");
            String presignedUrl = fileStorageService.getPresignedUrl(objectName);
            if (presignedUrl == null) {
                log.warn("Cannot get presigned URL for {}, using placeholder frames", objectName);
                return generatePlaceholderFrames(aiVideoId, fps, duration);
            }

            String ffmpeg = findFfmpeg();
            if (ffmpeg == null) {
                log.info("FFmpeg not found, using placeholder frames for video #{}", aiVideoId);
                return generatePlaceholderFrames(aiVideoId, fps, duration);
            }

            return extractWithFfmpeg(aiVideoId, presignedUrl, ffmpeg, tempDir, fps, duration);

        } catch (Exception e) {
            log.error("Frame extraction failed for video #{}: {}", aiVideoId, e.getMessage());
            return generatePlaceholderFrames(aiVideoId, fps, duration);
        } finally {
            if (tempDir != null) {
                try { deleteRecursively(tempDir); } catch (Exception ignored) {}
            }
        }
    }

    private int extractWithFfmpeg(Long aiVideoId, String videoUrl, String ffmpeg,
                                   Path tempDir, int fps, BigDecimal duration) throws Exception {
        // Extract evenly-spaced frames across the full video duration (1 frame per 2 seconds)
        String outputPattern = tempDir.resolve("frame_%04d.jpg").toString();
        List<String> cmd = Arrays.asList(
                ffmpeg,
                "-i", videoUrl,
                "-vf", "fps=0.5,scale=" + THUMBNAIL_WIDTH + ":-1",
                "-q:v", "2",
                "-frame_pts", "1",
                "-frames:v", String.valueOf(MAX_FRAMES),
                "-y",
                outputPattern
        );

        log.info("Running FFmpeg: {}", String.join(" ", cmd));
        Process process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .start();

        StringBuilder ffmpegOutput = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ffmpegOutput.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(120, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new BusinessException("FFmpeg timed out");
        }

        // Parse FFmpeg output for frame timestamps
        // FFmpeg with -frame_pts outputs lines like: "frame=   0 fps=0.0 q=2.0 ..."
        List<Double> timestamps = parseFfmpegTimestamps(ffmpegOutput.toString());

        // Get generated JPEG files
        File[] files = tempDir.toFile().listFiles((d, name) -> name.endsWith(".jpg"));
        if (files == null || files.length == 0) {
            log.warn("FFmpeg produced no frame files");
            return 0;
        }
        Arrays.sort(files);

        int count = 0;
        for (int i = 0; i < files.length && count < MAX_FRAMES; i++) {
            double timestamp = i < timestamps.size() ? timestamps.get(i) : (count * 1.0);
            int frameNum = (int) Math.round(timestamp * fps);

            // Upload thumbnail to MinIO
            String thumbnailObject = uploadThumbnail(files[i], aiVideoId, frameNum);

            VideoFrame frame = new VideoFrame();
            frame.setVideoId(aiVideoId);
            frame.setTimestamp(BigDecimal.valueOf(timestamp));
            frame.setFrameNumber(frameNum);
            frame.setThumbnailUrl(thumbnailObject);
            frame.setIsKeyframe(1);
            frame.setCreatedAt(LocalDateTime.now());
            videoFrameMapper.insert(frame);
            count++;
        }

        log.info("Extracted {} keyframes for video #{}", count, aiVideoId);
        return count;
    }

    private List<Double> parseFfmpegTimestamps(String output) {
        List<Double> timestamps = new ArrayList<>();
        for (String line : output.split("\n")) {
            // Parse "pts_time:12.345" or similar patterns
            if (line.contains("pts_time:")) {
                try {
                    int idx = line.indexOf("pts_time:") + 9;
                    String num = line.substring(idx).split("\\s+")[0].trim();
                    timestamps.add(Double.parseDouble(num));
                } catch (Exception ignored) {}
            }
        }
        return timestamps;
    }

    /**
     * Generate placeholder frames when FFmpeg is unavailable.
     * Creates evenly-spaced "virtual" frames with empty thumbnails.
     */
    private int generatePlaceholderFrames(Long aiVideoId, int fps, BigDecimal duration) {
        double dur = duration != null ? duration.doubleValue() : 5.0;
        int frameCount = Math.min(MAX_FRAMES, Math.max(3, (int) Math.ceil(dur)));

        for (int i = 0; i < frameCount; i++) {
            double timestamp = (i * dur) / frameCount;
            int frameNum = (int) Math.round(timestamp * Math.max(fps, 24));

            VideoFrame frame = new VideoFrame();
            frame.setVideoId(aiVideoId);
            frame.setTimestamp(BigDecimal.valueOf(Math.round(timestamp * 100.0) / 100.0));
            frame.setFrameNumber(frameNum);
            frame.setThumbnailUrl(null);
            frame.setIsKeyframe(i == 0 || i == frameCount - 1 ? 1 : 0);
            frame.setCreatedAt(LocalDateTime.now());
            videoFrameMapper.insert(frame);
        }
        log.info("Generated {} placeholder frames for video #{}", frameCount, aiVideoId);
        return frameCount;
    }

    private String uploadThumbnail(File file, Long aiVideoId, int frameNum) {
        try {
            String objectName = "frames/" + aiVideoId + "/thumb_" + frameNum + ".jpg";
            byte[] data = Files.readAllBytes(file.toPath());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType("image/jpeg")
                    .build());
            log.info("Thumbnail uploaded: {}/{}", bucket, objectName);
            // Return presigned URL so frontend can display thumbnails
            return fileStorageService.getPresignedUrl(objectName);
        } catch (Exception e) {
            log.warn("Failed to upload thumbnail for frame #{}: {}", frameNum, e.getMessage());
            return null;
        }
    }

    private String findFfmpeg() {
        if (ffmpegPath != null) return ffmpegPath;

        // 1. Check configured path from application.yml
        if (configuredFfmpegPath != null && !configuredFfmpegPath.isBlank()) {
            if (Files.exists(Path.of(configuredFfmpegPath))) {
                ffmpegPath = configuredFfmpegPath;
                log.info("FFmpeg found via config: {}", ffmpegPath);
                return ffmpegPath;
            }
            log.warn("Configured ffmpeg.path not found: {}", configuredFfmpegPath);
        }

        // 2. Check PATH
        for (String name : new String[]{"ffmpeg", "ffmpeg.exe"}) {
            try {
                Process p = new ProcessBuilder(name, "-version").redirectErrorStream(true).start();
                if (p.waitFor(3, TimeUnit.SECONDS) && p.exitValue() == 0) {
                    ffmpegPath = name;
                    log.info("FFmpeg found in PATH: {}", name);
                    return name;
                }
            } catch (Exception ignored) {}
        }

        // 3. Check common installation paths
        for (String path : new String[]{
                "C:\\ffmpeg\\bin\\ffmpeg.exe",
                "E:\\ffmpeg\\ffmpeg-8.1.1-essentials_build\\bin\\ffmpeg.exe",
                "E:\\ffmepg\\ffmpeg-8.1.1-essentials_build\\bin\\ffmpeg.exe",
                "/usr/bin/ffmpeg",
                "/usr/local/bin/ffmpeg"
        }) {
            if (Files.exists(Path.of(path))) {
                ffmpegPath = path;
                log.info("FFmpeg found at: {}", path);
                return path;
            }
        }

        log.info("FFmpeg not found — will use placeholder frames");
        return null;
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                stream.forEach(p -> {
                    try { deleteRecursively(p); } catch (Exception ignored) {}
                });
            }
        }
        Files.deleteIfExists(path);
    }
}
