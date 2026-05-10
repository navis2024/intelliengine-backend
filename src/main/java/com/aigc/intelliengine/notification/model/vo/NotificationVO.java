package com.aigc.intelliengine.notification.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "通知信息")
public class NotificationVO {
    private String id;
    private String title;
    private String content;
    private String type;
    private Integer isRead;
    private Long relatedId;
    private LocalDateTime createdAt;
}
