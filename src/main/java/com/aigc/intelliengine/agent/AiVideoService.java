package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.dto.AiVideoCreateRequest;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.FileStorageService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final FileStorageService fileStorageService;
    private final LlmConfig llmConfig;
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

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert AIGC video production advisor. Review this project and suggest improvements.\n\n");
        prompt.append("=== MAIN PROMPT ===\n").append(video.getPromptText()).append("\n\n");

        if (!frames.isEmpty()) {
            prompt.append("=== FRAME DESCRIPTIONS ===\n");
            for (VideoFrame f : frames) {
                prompt.append("Frame #").append(f.getFrameNumber()).append(" [").append(f.getTimestamp()).append("s]: ")
                      .append(f.getPromptText() != null ? f.getPromptText() : "(no description)").append("\n");
            }
            prompt.append("\n");
        }

        if (!comments.isEmpty()) {
            prompt.append("=== REVIEW COMMENTS ===\n");
            for (ReviewComment c : comments) {
                prompt.append("- [").append(c.getCommentType()).append("] ").append(c.getContent());
                if (c.getFrameNumber() != null) prompt.append(" [frame ").append(c.getFrameNumber()).append("]");
                prompt.append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("=== TASK ===\n");
        prompt.append("1. Analyze what needs improvement based on review feedback\n");
        prompt.append("2. Write an optimized version of the main prompt\n");
        prompt.append("3. List 3-5 actionable suggestions for the next version\n");
        prompt.append("Respond ONLY as JSON (no markdown fences):\n");
        prompt.append("{\"analysis\":\"...\",\"improvedPrompt\":\"...\",\"suggestions\":[\"s1\",\"s2\"],\"priorityAreas\":[\"a1\"],\"confidence\":0.85}");

        String agentAdvice, improvedPrompt;
        try {
            String llmResp = callLlmForAdvice(prompt.toString());
            String content = new ObjectMapper().readTree(llmResp)
                    .path("choices").get(0).path("message").path("content").asText();
            if (content.startsWith("```json")) content = content.substring(7, content.length() - 3).trim();
            else if (content.startsWith("```")) content = content.substring(3, content.length() - 3).trim();
            agentAdvice = content;
            improvedPrompt = new ObjectMapper().readTree(content).path("improvedPrompt").asText(video.getPromptText());
        } catch (Exception e) {
            log.warn("LLM call failed for generateNextVersion: {}", e.getMessage());
            improvedPrompt = video.getPromptText() + " [AI-enhanced]";
            agentAdvice = "{\"analysis\":\"LLM unavailable\",\"improvedPrompt\":\"" +
                    improvedPrompt.replace("\"", "'") + "\",\"suggestions\":[],\"confidence\":0.5}";
        }

        AssetInfo asset = assetMapper.selectById(video.getAssetId());
        if (asset != null) {
            int nextVersion = (asset.getVersion() != null ? asset.getVersion() : 1) + 1;
            asset.setVersion(nextVersion);
            asset.setCommitMessage("AI-enhanced v" + nextVersion);
            asset.setUpdatedAt(LocalDateTime.now());
            assetMapper.updateById(asset);

            AssetVersion ver = new AssetVersion();
            ver.setAssetId(asset.getId());
            ver.setVersionNumber(nextVersion);
            ver.setChangeLog("Agent generated version — " + comments.size() + " reviews incorporated");
            ver.setSnapshotData("{\"prompt\":\"" + improvedPrompt.replace("\"", "'") + "\"}");
            ver.setAgentAdvice(agentAdvice);
            ver.setCreatedBy(userId);
            ver.setCreatedAt(LocalDateTime.now());
            assetVersionMapper.insert(ver);
        }

        video.setPromptText(improvedPrompt);
        aiVideoMapper.updateById(video);

        return Map.of(
                "videoId", videoId, "assetId", video.getAssetId(),
                "newPrompt", improvedPrompt, "agentAdvice", agentAdvice,
                "version", asset != null ? asset.getVersion() : 1,
                "commentsIncorporated", comments.size(), "framesAnalyzed", frames.size());
    }

    private String callLlmForAdvice(String userPrompt) {
        RestTemplate rt = new RestTemplate();
        rt.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(30_000);
            setReadTimeout(90_000);
        }});
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + llmConfig.getApiKey());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", llmConfig.getModel());
        body.put("temperature", llmConfig.getTemperature());
        body.put("max_tokens", llmConfig.getMaxTokens());
        body.put("messages", List.of(
                Map.of("role", "system", "content", "You are an expert AIGC video production assistant. Respond with valid JSON only."),
                Map.of("role", "user", "content", userPrompt)));
        ResponseEntity<String> resp = rt.postForEntity(
                llmConfig.getBaseUrl() + "/chat/completions", new HttpEntity<>(body, headers), String.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) return resp.getBody();
        throw new RuntimeException("LLM API returned " + resp.getStatusCode());
    }

    public String getThumbnailPresignedUrl(String objectPath) {
        return fileStorageService.getPresignedUrl(objectPath);
    }

    public byte[] readThumbnailBytes(String objectPath) {
        try {
            String presigned = fileStorageService.getPresignedUrl(objectPath);
            if (presigned == null) return null;
            java.net.URL url = new java.net.URL(presigned);
            try (java.io.InputStream is = url.openStream();
                 java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.warn("Failed to read thumbnail {}: {}", objectPath, e.getMessage());
            return null;
        }
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
                String objectPath = "frames/" + videoId + "/thumb_" + f.getFrameNumber() + ".jpg";
                byte[] imgBytes = readThumbnailBytes(objectPath);
                if (imgBytes != null && imgBytes.length > 100) {
                    String base64 = java.util.Base64.getEncoder().encodeToString(imgBytes);
                    String desc = callVisionApi(base64, f.getFrameNumber(), f.getTimestamp());
                    if (desc != null && !desc.isBlank()) {
                        f.setPromptText(desc.trim());
                        videoFrameMapper.updateById(f);
                        analyzed++;
                        log.info("Vision analyzed frame #{}: {}", f.getFrameNumber(), desc.substring(0, Math.min(50, desc.length())));
                    }
                }
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            }
        }
        return Map.of("videoId", videoId, "totalFrames", frames.size(), "analyzedFrames", analyzed);
    }

    private String callVisionApi(String base64Image, int frameNum, java.math.BigDecimal timestamp) {
        try {
            String ctx = "帧#" + frameNum + " 时间" + timestamp + "秒";
            Map<String, Object> systemMsg = Map.of("role", "system", "content",
                    "你是一个专业的视频画面分析助手。请用简洁的中文描述视频帧的画面内容，包括：镜头类型、场景、主体、光线、色调。50字以内。");
            Map<String, Object> userMsg = Map.of("role", "user", "content", java.util.List.of(
                    Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image)),
                    Map.of("type", "text", "text", "描述这个视频帧的画面内容(" + ctx + ")：")
            ));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", llmConfig.getModel());
            body.put("temperature", 1.0);
            body.put("max_tokens", 500);
            body.put("messages", java.util.List.of(systemMsg, userMsg));

            RestTemplate rt = new RestTemplate();
            rt.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                setConnectTimeout(30_000); setReadTimeout(120_000);
            }});
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + llmConfig.getApiKey());

            ResponseEntity<String> resp = rt.postForEntity(
                    llmConfig.getBaseUrl() + "/chat/completions",
                    new HttpEntity<>(new ObjectMapper().writeValueAsString(body), headers), String.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                var root = new ObjectMapper().readTree(resp.getBody());
                var msg = root.path("choices").get(0).path("message");
                String content = msg.path("content").asText();
                if (content.isBlank()) content = msg.path("reasoning_content").asText();
                return content.isBlank() ? null : content.trim();
            }
        } catch (Exception e) {
            log.warn("Vision API failed for frame #{}: {}", frameNum, e.getMessage());
        }
        return null;
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
