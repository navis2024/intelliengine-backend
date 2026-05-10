package com.aigc.intelliengine.agent.tools;

import com.aigc.intelliengine.agent.VideoFrameExtractionService;
import com.aigc.intelliengine.agent.model.entity.AssetAiVideo;
import com.aigc.intelliengine.agent.AiVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExtractFramesTool implements AgentTool {

    private final AiVideoService aiVideoService;
    private final VideoFrameExtractionService extractionService;

    @Override public String name() { return "extract_frames"; }

    @Override
    public String description() {
        return "从AI视频中提取关键帧(I-frame)。输入videoId，返回抽取的帧数。视频分析前必须先调用此工具。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "videoId", Map.of("type", "integer", "description", "AI视频ID")
            ),
            "required", java.util.List.of("videoId")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        long videoId = ((Number) args.get("videoId")).longValue();
        AssetAiVideo video = aiVideoService.findByAssetId(videoId, null);
        if (video == null) return "错误: AI视频 #" + videoId + " 不存在";

        int count = extractionService.extractFrames(videoId, video.getOriginalUrl(),
                video.getFps() != null ? video.getFps() : 24, null);
        return String.format("从视频 #%d 中成功抽取 %d 个关键帧", videoId, count);
    }
}
