package com.aigc.intelliengine.user.adapter.web.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 *
 * 返回JWT Token和用户信息
 *
 * @author 智摩开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * JWT Token
     */
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    /**
     * Token有效期(秒)
     */
    @Schema(description = "Token有效期(秒)", example = "7200")
    private Integer expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserVO user;
}
