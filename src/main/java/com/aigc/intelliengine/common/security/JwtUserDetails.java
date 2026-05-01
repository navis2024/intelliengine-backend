package com.aigc.intelliengine.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * JWT用户详情
 * 用于Spring Security存储认证用户信息
 */
@Data
@AllArgsConstructor
public class JwtUserDetails implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
}
