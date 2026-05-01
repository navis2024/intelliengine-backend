package com.aigc.intelliengine.user.adapter.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * <p>
 * 接口: POST /api/v1/users/register
 * 限流: 同一IP每分5次
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterRequest {

    /**
     * 用户名
     * <p>
     * 要求: 6-20个字符，字母开头，只能包含字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字、下划线")
    @Schema(
            description = "用户名",
            example = "zhangsan123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6,
            maxLength = 20,
            pattern = "^[a-zA-Z][a-zA-Z0-9_]*$"
    )
    private String username;

    /**
     * 密码
     * <p>
     * 要求: 6-20个字符
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(
            description = "密码",
            example = "Password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6,
            maxLength = 20
    )
    private String password;

    /**
     * 邮箱（可选）
     */
    @Email(message = "邮箱格式不正确")
    @Schema(
            description = "邮箱（可选）",
            example = "zhangsan@example.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    /**
     * 手机号（可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(
            description = "手机号（可选）",
            example = "13800138000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            pattern = "^1[3-9]\\d{9}$"
    )
    private String phone;

    /**
     * 昵称（可选）
     */
    @Schema(
            description = "昵称（可选）",
            example = "张三",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String nickname;
}
