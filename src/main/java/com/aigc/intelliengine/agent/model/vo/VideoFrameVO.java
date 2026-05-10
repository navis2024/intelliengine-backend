package com.aigc.intelliengine.agent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "视频帧信息")
public class VideoFrameVO {
    private Long id;
    private Long videoId;
    private BigDecimal timestamp;
    private Integer frameNumber;
    private String thumbnailUrl;
    private String promptText;
    private String parameters;
    private Integer isKeyframe;
    private String tags;
    private LocalDateTime createdAt;
}
