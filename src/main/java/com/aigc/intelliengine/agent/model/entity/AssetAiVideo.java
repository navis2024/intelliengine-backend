package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("asset_ai_video")
public class AssetAiVideo {
    @TableId(type = IdType.AUTO)
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
