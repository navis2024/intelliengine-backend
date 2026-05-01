package com.aigc.intelliengine.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供Token生成、解析、验证功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    
    private final JwtConfig jwtConfig;
    private SecretKey secretKey;
    
    @PostConstruct
    public void init() {
        // 使用配置的密钥生成SecretKey
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成JWT Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration());
        
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 从Token中解析用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null && claims.get("userId") != null) {
            return Long.valueOf(claims.get("userId").toString());
        }
        return null;
    }
    
    /**
     * 从Token中解析用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null && claims.get("username") != null) {
            return claims.get("username").toString();
        }
        return null;
    }
    
    /**
     * 解析Token
     * 
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或非法: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return true表示有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null && !isTokenExpired(claims);
    }
    
    /**
     * 检查Token是否过期
     * 
     * @param claims Claims对象
     * @return true表示已过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
    
    /**
     * 获取Token过时时间（秒）
     * <p>
     * 计算Token距离过期的剩余时间，用于登出时设置黑名单过期时间
     *
     * @param token JWT Token
     * @return 剩余过期时间（秒），已过期返回0
     */
    public Long getExpirationDateFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Date expiration = claims.getExpiration();
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        }
        return 0L;
    }
}
