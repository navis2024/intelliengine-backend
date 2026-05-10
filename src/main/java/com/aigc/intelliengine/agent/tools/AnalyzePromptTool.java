package com.aigc.intelliengine.agent.tools;

import com.aigc.intelliengine.agent.*;
import com.aigc.intelliengine.agent.model.entity.VideoFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalyzePromptTool implements AgentTool {

    private final PromptAnalysisService promptAnalysisService;
    private final AiVideoService aiVideoService;

    @Override public String name() { return "analyze_prompt"; }

    @Override
    public String description() {
        return "分析视频帧的AIGC提示词质量。输入frameId，返回增强版prompt、分类标签和置信度。当需要评估或优化prompt时使用。";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "frameId", Map.of("type", "integer", "description", "视频帧ID")
            ),
            "required", java.util.List.of("frameId")
        );
    }

    @Override
    public String execute(Map<String, Object> args) {
        long frameId = ((Number) args.get("frameId")).longValue();
        VideoFrame frame = aiVideoService.findFrameById(frameId);
        if (frame == null) return "错误: 帧 #" + frameId + " 不存在";

        AnalysisResult result = promptAnalysisService.analyzeFrame(frame);
        return String.format("帧 #%d 分析完成: 置信度=%.2f, 标签=%s, 增强prompt=%s",
                result.getFrameNumber(), result.getConfidence(),
                String.join(",", result.getSuggestedTags()),
                result.getAnalyzedPrompt());
    }
}
