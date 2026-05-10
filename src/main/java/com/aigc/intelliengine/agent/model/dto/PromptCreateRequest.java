package com.aigc.intelliengine.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建Prompt请求")
public class PromptCreateRequest {
    @NotBlank @Schema(description = "提示词文本", required = true)
    private String promptText;
    @Schema(description = "提示词类型")
    private String promptType;
    @Schema(description = "风格标签,逗号分隔")
    private String styleTags;
    @Schema(description = "来源视频ID")
    private Long sourceVideoId;
    @Schema(description = "来源帧ID")
    private Long sourceFrameId;
}
