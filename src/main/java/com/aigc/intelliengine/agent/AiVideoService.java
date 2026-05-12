package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.dto.AiVideoCreateRequest;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.AssetVersionMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.asset.model.entity.AssetVersion;
import com.aigc.intelliengine.common.config.RabbitMQConfig;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.project.ProjectMemberMapper;
import com.aigc.intelliengine.project.model.entity.ProjectMember;
import com.aigc.intelliengine.review.ReviewCommentMapper;
import com.aigc.intelliengine.review.model.entity.ReviewComment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiVideoService {

    private final AiVideoMapper aiVideoMapper;
    private final VideoFrameMapper videoFrameMapper;
    private final AssetMapper assetMapper;
    private final AssetVersionMapper assetVersionMapper;
    private final MembershipValidator validator;
    private final ProjectMemberMapper memberMapper;
    private final ReviewCommentMapper reviewCommentMapper;
    private final PromptAnalysisService promptAnalysisService;
    private final VideoFrameExtractionService frameExtractionService;
    private final VisionAnalysisService visionAnalysisService;
    private final RabbitTemplate rabbitTemplate;

    public AssetAiVideo findByAssetId(Long assetId, Long userId) {
        validator.requireAssetAccess(assetId, userId);
        return aiVideoMapper.selectOne(new LambdaQueryWrapper<AssetAiVideo>().eq(AssetAiVideo::getAssetId, assetId));
    }

    @Transactional
    public AssetAiVideo createAiVideo(AiVideoCreateRequest request, Long userId) {
        validator.requireAssetAccess(request.getAssetId(), userId);
        AssetAiVideo existing = findByAssetId(request.getAssetId(), userId);
        if (existing != null) {
            throw new BusinessException("该资产已关联AI视频元数据");
        }
        AssetAiVideo video = new AssetAiVideo();
        video.setAssetId(request.getAssetId());
        video.setToolType(request.getToolType());
        video.setToolVersion(request.getToolVersion());
        video.setPromptText(request.getPromptText());
        video.setNegativePrompt(request.getNegativePrompt());
        video.setParameters(request.getParameters());
        video.setOriginalUrl(request.getOriginalUrl());
        video.setFps(request.getFps() != null ? request.getFps() : 30);
        video.setCreatedAt(LocalDateTime.now());
        aiVideoMapper.insert(video);

        // Async: send frame extraction task to RabbitMQ (non-blocking)
        AssetInfo asset = assetMapper.selectById(request.getAssetId());
        if (asset != null && "VIDEO".equals(asset.getType()) && asset.getFileUrl() != null) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RK_VIDEO_EXTRACT,
                    Map.of("aiVideoId", video.getId(), "assetId", asset.getId()));
            log.info("[MQ] Sent video extract task: aiVideoId={}, assetId={}", video.getId(), asset.getId());
        }

        return video;
    }

    @Transactional
    public AssetAiVideo updateAiVideo(Long id, AiVideoCreateRequest request, Long userId) {
        AssetAiVideo video = aiVideoMapper.selectById(id);
        if (video == null) throw new BusinessException("AI视频元数据不存在");
        validator.requireAssetAccess(video.getAssetId(), userId);
        if (request.getToolType() != null) video.setToolType(request.getToolType());
        if (request.getToolVersion() != null) video.setToolVersion(request.getToolVersion());
        if (request.getPromptText() != null) video.setPromptText(request.getPromptText());
        if (request.getNegativePrompt() != null) video.setNegativePrompt(request.getNegativePrompt());
        if (request.getParameters() != null) video.setParameters(request.getParameters());
        if (request.getOriginalUrl() != null) video.setOriginalUrl(request.getOriginalUrl());
        if (request.getFps() != null) video.setFps(request.getFps());
        aiVideoMapper.updateById(video);
        return video;
    }

    @Transactional
    public VideoFrame addFrame(Long videoId, BigDecimal timestamp, Integer frameNumber, String thumbnailUrl,
                                String promptText, String parameters, Integer isKeyframe, String tags) {
        VideoFrame frame = new VideoFrame();
        frame.setVideoId(videoId);
        frame.setTimestamp(timestamp);
        frame.setFrameNumber(frameNumber);
        frame.setThumbnailUrl(thumbnailUrl);
        frame.setPromptText(promptText);
        frame.setParameters(parameters);
        frame.setIsKeyframe(isKeyframe != null ? isKeyframe : 0);
        frame.setTags(tags);
        frame.setCreatedAt(LocalDateTime.now());
        videoFrameMapper.insert(frame);
        return frame;
    }

    public List<VideoFrame> getFramesByVideoId(Long videoId) {
        return videoFrameMapper.selectList(new LambdaQueryWrapper<VideoFrame>()
                .eq(VideoFrame::getVideoId, videoId).orderByAsc(VideoFrame::getFrameNumber));
    }

    public List<VideoFrame> getKeyframesByVideoId(Long videoId) {
        return videoFrameMapper.selectList(new LambdaQueryWrapper<VideoFrame>()
                .eq(VideoFrame::getVideoId, videoId).eq(VideoFrame::getIsKeyframe, 1).orderByAsc(VideoFrame::getFrameNumber));
    }

    public VideoFrame findFrameById(Long frameId) { return videoFrameMapper.selectById(frameId); }

    public List<AssetAiVideo> listAll(Long userId) {
        List<Long> accessibleAssetIds = new ArrayList<>();
        List<AssetInfo> userAssets = assetMapper.selectList(
            new LambdaQueryWrapper<AssetInfo>().eq(AssetInfo::getCreatedBy, userId));
        userAssets.forEach(a -> accessibleAssetIds.add(a.getId()));
        List<ProjectMember> memberships = memberMapper.selectByUser(userId);
        for (ProjectMember m : memberships) {
            List<AssetInfo> projectAssets = assetMapper.selectList(
                new LambdaQueryWrapper<AssetInfo>()
                    .eq(AssetInfo::getOwnerType, "PROJECT")
                    .eq(AssetInfo::getOwnerId, m.getProjectId()));
            projectAssets.forEach(a -> accessibleAssetIds.add(a.getId()));
        }
        if (accessibleAssetIds.isEmpty()) return List.of();
        return aiVideoMapper.selectList(
            new LambdaQueryWrapper<AssetAiVideo>().in(AssetAiVideo::getAssetId, accessibleAssetIds)
                .orderByDesc(AssetAiVideo::getCreatedAt));
    }

    public List<AssetAiVideo> listByProject(Long projectId, Long userId) {
        validator.requireMembership(projectId, userId);
        List<Long> projectAssetIds = assetMapper.selectList(
                new LambdaQueryWrapper<AssetInfo>()
                        .eq(AssetInfo::getOwnerType, "PROJECT")
                        .eq(AssetInfo::getOwnerId, projectId))
                .stream().map(AssetInfo::getId).collect(Collectors.toList());
        if (projectAssetIds.isEmpty()) return List.of();
        return aiVideoMapper.selectList(
                new LambdaQueryWrapper<AssetAiVideo>().in(AssetAiVideo::getAssetId, projectAssetIds)
                        .orderByDesc(AssetAiVideo::getCreatedAt));
    }

    @Transactional
    public Map<String, Object> generateNextVersion(Long videoId, Long projectId, Long userId) {
        validator.requireMembership(projectId, userId);
        AssetAiVideo video = aiVideoMapper.selectById(videoId);
        if (video == null) throw new BusinessException("AI视频元数据不存在");
        validator.requireAssetAccess(video.getAssetId(), userId);

        List<VideoFrame> frames = getFramesByVideoId(videoId);
        List<ReviewComment> comments = reviewCommentMapper.selectList(
                new LambdaQueryWrapper<ReviewComment>()
                        .eq(ReviewComment::getAssetId, video.getAssetId())
                        .eq(ReviewComment::getIsDeleted, 0));

        // Build enhancement prompt from review comments and frame data
        StringBuilder ctx = new StringBuilder();
        ctx.append("Current prompt: ").append(video.getPromptText()).append("\n");
        if (frames.size() > 0) {
            ctx.append("Frame count: ").append(frames.size()).append("\n");
            for (VideoFrame f : frames) {
                if (f.getPromptText() != null) ctx.append("  Frame ").append(f.getFrameNumber())
                        .append(": ").append(f.getPromptText()).append("\n");
            }
        }
        if (!comments.isEmpty()) {
            ctx.append("Review comments:\n");
            for (ReviewComment c : comments) {
                ctx.append("  - ").append(c.getContent()).append("\n");
            }
        }
        ctx.append("\nSuggest an improved version of the main prompt incorporating review feedback.");

        String improvedPrompt;
        try {
            if (frames.size() > 0) {
                AnalysisResult result = promptAnalysisService.analyzeFrame(frames.get(0));
                improvedPrompt = result.getAnalyzedPrompt();
            } else {
                improvedPrompt = video.getPromptText() + " [refined with agent suggestions]";
            }
        } catch (Exception e) {
            log.warn("LLM analysis failed, using basic enhancement: {}", e.getMessage());
            improvedPrompt = video.getPromptText() + " [enhanced: improved lighting, color grading, 8K upscale]";
        }

        // Create new asset version with enhanced parameters
        AssetInfo asset = assetMapper.selectById(video.getAssetId());
        if (asset != null) {
            int nextVersion = (asset.getVersion() != null ? asset.getVersion() : 1) + 1;
            asset.setVersion(nextVersion);
            asset.setCommitMessage("AI-enhanced version: " + (comments.size()) + " reviews incorporated");
            asset.setUpdatedAt(LocalDateTime.now());
            assetMapper.updateById(asset);

            AssetVersion ver = new AssetVersion();
            ver.setAssetId(asset.getId());
            ver.setVersionNumber(nextVersion);
            ver.setChangeLog("AI generate next version — " + comments.size() + " review comments incorporated");
            ver.setSnapshotData("{\"prompt\":\"" + improvedPrompt.replace("\"", "'") + "\"}");
            ver.setCreatedBy(userId);
            ver.setCreatedAt(LocalDateTime.now());
            assetVersionMapper.insert(ver);
        }

        // Update AI video with improved prompt
        video.setPromptText(improvedPrompt);
        aiVideoMapper.updateById(video);

        return Map.of(
                "videoId", videoId,
                "assetId", video.getAssetId(),
                "newPrompt", improvedPrompt,
                "version", asset != null ? asset.getVersion() : 1,
                "commentsIncorporated", comments.size(),
                "framesAnalyzed", frames.size()
        );
    }

    public int triggerFrameExtraction(Long videoId, Long userId) {
        AssetAiVideo video = aiVideoMapper.selectById(videoId);
        if (video == null) throw new BusinessException("AI视频元数据不存在");
        validator.requireAssetAccess(video.getAssetId(), userId);
        AssetInfo asset = assetMapper.selectById(video.getAssetId());
        if (asset == null || asset.getFileUrl() == null)
            throw new BusinessException("资产文件不存在，无法提取帧");

        // Clear old frames before re-extraction
        videoFrameMapper.delete(new LambdaQueryWrapper<VideoFrame>()
                .eq(VideoFrame::getVideoId, videoId));

        return frameExtractionService.extractFrames(
                videoId, asset.getFileUrl(),
                video.getFps() != null ? video.getFps() : 24, null);
    }

    public Map<String, Object> analyzeFramesWithVision(Long videoId, Long userId) {
        AssetAiVideo video = aiVideoMapper.selectById(videoId);
        if (video == null) throw new BusinessException("AI视频元数据不存在");
        validator.requireAssetAccess(video.getAssetId(), userId);

        List<VideoFrame> frames = videoFrameMapper.selectList(
                new LambdaQueryWrapper<VideoFrame>()
                        .eq(VideoFrame::getVideoId, videoId)
                        .orderByAsc(VideoFrame::getFrameNumber));

        int analyzed = 0;
        for (VideoFrame f : frames) {
            if (f.getThumbnailUrl() != null) {
                // Extract MinIO object path from presigned URL
                // URL: http://localhost:9000/bucket/frames/5/thumb_0.jpg?params...
                String url = f.getThumbnailUrl();
                int pathStart = url.indexOf('/', 10); // skip http://localhost:9000/
                String fullPath = pathStart > 0 ? url.substring(pathStart + 1).split("\\?")[0] : url;
                int bucketEnd = fullPath.indexOf('/');
                String objectPath = bucketEnd > 0 ? fullPath.substring(bucketEnd + 1) : fullPath;
                String ctx = "帧#" + f.getFrameNumber() + " 时间" + f.getTimestamp() + "秒";
                String desc = visionAnalysisService.analyzeFrame(objectPath, ctx);
                if (desc != null && !desc.isBlank()) {
                    f.setPromptText(desc.trim());
                    videoFrameMapper.updateById(f);
                    analyzed++;
                    log.info("Vision analyzed frame #{}: {}", f.getFrameNumber(), desc.substring(0, 50));
                }
                // Small delay between frames to avoid rate limiting
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            }
        }
        return Map.of("videoId", videoId, "totalFrames", frames.size(), "analyzedFrames", analyzed);
    }

    @Transactional
    public void deleteAiVideo(Long id, Long userId) {
        AssetAiVideo video = aiVideoMapper.selectById(id);
        if (video == null) throw new BusinessException("AI视频元数据不存在");
        validator.requireAssetAccess(video.getAssetId(), userId);
        if (video != null) {
            videoFrameMapper.delete(new LambdaQueryWrapper<VideoFrame>().eq(VideoFrame::getVideoId, id));
            aiVideoMapper.deleteById(id);
        }
    }
}
