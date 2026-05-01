package com.aigc.intelliengine.review.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审阅回复数据对象
 * 对应表: review_reply
 */
@Data
@TableName("review_reply")
public class ReviewReplyDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private String content;
    private Long createdBy;
    private LocalDateTime createdAt;
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
