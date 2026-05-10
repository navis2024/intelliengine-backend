package com.aigc.intelliengine.agent;

import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.asset.AssetMapper;
import com.aigc.intelliengine.asset.model.entity.AssetInfo;
import com.aigc.intelliengine.common.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoFrameWorker {

    private final VideoFrameExtractionService extractionService;
    private final VideoFrameMapper videoFrameMapper;
    private final AiVideoMapper aiVideoMapper;
    private final AssetMapper assetMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_VIDEO_EXTRACT)
    public void handleVideoExtract(Map<String, Object> message) {
        Long aiVideoId = toLong(message.get("aiVideoId"));
        Long assetId = toLong(message.get("assetId"));

        log.info("[MQ] VideoFrameWorker received: aiVideoId={}, assetId={}", aiVideoId, assetId);

        AssetAiVideo video = aiVideoMapper.selectById(aiVideoId);
        if (video == null) {
            log.error("[MQ] AI video not found: {}", aiVideoId);
            return;
        }

        AssetInfo asset = assetMapper.selectById(assetId);
        if (asset == null || asset.getFileUrl() == null) {
            log.warn("[MQ] Asset {} has no file URL, skipping extraction", assetId);
            return;
        }

        try {
            int count = extractionService.extractFrames(
                    aiVideoId, asset.getFileUrl(),
                    video.getFps() != null ? video.getFps() : 30, null);
            log.info("[MQ] Extracted {} frames for AI video #{}", count, aiVideoId);
        } catch (Exception e) {
            log.error("[MQ] Frame extraction failed for video #{}: {}", aiVideoId, e.getMessage());
            throw new RuntimeException("Frame extraction failed", e);
        }
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        if (val instanceof String s) return Long.valueOf(s);
        return null;
    }
}
