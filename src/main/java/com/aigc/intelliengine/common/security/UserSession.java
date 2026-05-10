package com.aigc.intelliengine.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession implements Serializable {
    private Long userId;
    private String username;
    private Long loginTime;
    private String token;
}
