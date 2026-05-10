package com.aigc.intelliengine.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

    @Schema(description = "Token有效期(秒)", example = "7200")
    private Integer expiresIn;

    @Schema(description = "用户信息")
    private UserVO user;
}
