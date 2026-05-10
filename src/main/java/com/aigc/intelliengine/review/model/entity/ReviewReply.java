package com.aigc.intelliengine.review.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review_reply")
public class ReviewReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private String content;
    private Long createdBy;
    private LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
