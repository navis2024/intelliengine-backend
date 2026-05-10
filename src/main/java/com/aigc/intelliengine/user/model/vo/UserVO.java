package com.aigc.intelliengine.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1789456123456789123")
    private String id;

    @Schema(description = "用户名", example = "zhangsan123")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "状态(0-禁用 1-正常 2-未激活)", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
