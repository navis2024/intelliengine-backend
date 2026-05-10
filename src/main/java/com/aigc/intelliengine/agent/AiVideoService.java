package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.dto.AiVideoCreateRequest;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.common.config.RabbitMQConfig;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.project.ProjectMemberMapper;
import com.aigc.intelliengine.project.model.entity.ProjectMember;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AiVideoService {

    private final AiVideoMapper aiVideoMapper;
    private final VideoFrameMapper videoFrameMapper;
    private final AssetMapper assetMapper;
    private final MembershipValidator validator;
    private final ProjectMemberMapper memberMapper;
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
