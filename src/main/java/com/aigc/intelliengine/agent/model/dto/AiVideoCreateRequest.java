package com.aigc.intelliengine.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建AI视频元数据请求")
public class AiVideoCreateRequest {
    @NotNull
    @Schema(description = "关联的资产ID", required = true)
    private Long assetId;

    @NotBlank
    @Schema(description = "AI工具类型: RUNWAY/JIMENG/MIDJOURNEY/SORA", example = "RUNWAY")
    private String toolType;

    @Schema(description = "工具版本", example = "Gen-3")
    private String toolVersion;

    @Schema(description = "正向提示词")
    private String promptText;

    @Schema(description = "反向提示词")
    private String negativePrompt;

    @Schema(description = "生成参数JSON", example = "{\"seed\":12345,\"steps\":50,\"cfg\":7.5}")
    private String parameters;

    @Schema(description = "原始平台URL")
    private String originalUrl;

    @Schema(description = "帧率")
    private Integer fps;
}
