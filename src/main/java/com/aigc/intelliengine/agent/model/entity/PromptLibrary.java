package com.aigc.intelliengine.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("prompt_library")
public class PromptLibrary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String promptText;
    private String promptType;
    private String styleTags;
    private Long sourceVideoId;
    private Long sourceFrameId;
    private Long createdBy;
    private Integer useCount;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
