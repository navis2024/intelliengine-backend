package com.aigc.intelliengine.user.adapter.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 *
 * 接口: POST /api/v1/users/login
 *
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    /**
     * 登录账号（用户名/邮箱/手机号）
     */
    @NotBlank(message = "登录账号不能为空")
    @Schema(description = "用户名/邮箱/手机号", example = "zhangsan123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
