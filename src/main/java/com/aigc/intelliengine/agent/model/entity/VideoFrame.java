package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("video_frame")
public class VideoFrame {
    @TableId(type = IdType.AUTO)
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
