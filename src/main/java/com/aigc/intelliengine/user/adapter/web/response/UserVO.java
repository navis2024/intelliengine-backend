package com.aigc.intelliengine.user.adapter.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息响应VO
 *
 * 用于返回用户基本信息（不包含敏感信息如密码）
 *
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（字符串格式）
     */
    @Schema(description = "用户ID", example = "1789456123456789123")
    private String id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhangsan123")
    private String username;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    /**
     * 状态
     * 0-禁用, 1-正常, 2-未激活
     */
    @Schema(description = "状态(0-禁用 1-正常 2-未激活)", example = "1")
    private Integer status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01T12:00:00")
    private LocalDateTime updateTime;
}
