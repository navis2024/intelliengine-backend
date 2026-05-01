package com.aigc.intelliengine.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 * 从application.yml读取jwt相关配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secret;
    
    /**
     * Token过期时间(毫秒)
     * 默认7天
     */
    private Long expiration = 604800000L;
    
    /**
     * Token请求头名称
     */
    private String header = "Authorization";
    
    /**
     * Token前缀
     */
    private String prefix = "Bearer ";
}
