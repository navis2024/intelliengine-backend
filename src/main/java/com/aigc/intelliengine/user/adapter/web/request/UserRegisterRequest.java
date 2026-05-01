package com.aigc.intelliengine.user.adapter.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 *
 * 接口: POST /api/v1/users/register
 *
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest {

    /**
     * 用户名
     * 规则: 6-20位，字母数字下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度必须在6-20位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "用户名（6-20位，字母数字下划线）", example = "zhangsan123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密码
     * 规则: 6-20位
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "密码（6-20位）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱（可选）", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号（可选）
     * 规则: 中国大陆手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号（可选）", example = "13800138000")
    private String phone;
}
