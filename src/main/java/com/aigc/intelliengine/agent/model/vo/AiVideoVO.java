package com.aigc.intelliengine.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "AI视频信息")
public class AiVideoVO {
    private Long id;
    private Long assetId;
    private String toolType;
    private String toolVersion;
    private String promptText;
    private String negativePrompt;
    private String parameters;
    private String originalUrl;
    private Integer fps;
    private LocalDateTime createdAt;
}
