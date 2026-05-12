package com.aigc.intelliengine.notification.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("recipient_id")
    private Long userId;
    private String title;
    private String content;
    @TableField("notification_type")
    private String type;
    private Integer isRead;
    @TableField("related_type")
    private String relatedType;
    private Long relatedId;
    private LocalDateTime createdAt;
}
