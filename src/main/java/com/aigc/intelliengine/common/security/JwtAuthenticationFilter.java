package com.aigc.intelliengine.common.security;

import com.aigc.intelliengine.common.redis.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_PREFIX = "session:user:";

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final TokenBlacklistService tokenBlacklistService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtConfig jwtConfig,
                                   TokenBlacklistService tokenBlacklistService,
                                   StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
        this.tokenBlacklistService = tokenBlacklistService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestPath = request.getRequestURI();

            if (isPublicPath(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.warn("Token已被加入黑名单，拒绝访问 - 路径: {}", requestPath);
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                if (userId != null) {
                    // Populate Spring Security context
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            new JwtUserDetails(userId, username),
                            null,
                            new ArrayList<>()
                        );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Populate ThreadLocal via UserContextHolder (Redis shared session)
                    UserSession session = loadOrCreateSession(userId, username, token);
                    UserContextHolder.set(session);

                    // Legacy: keep request attributes for backward compatibility
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);

                    log.debug("JWT认证成功 - 用户: {}, 路径: {}", username, requestPath);
                }
            } else {
                log.debug("JWT认证失败或Token缺失 - 路径: {}", requestPath);
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }

    private UserSession loadOrCreateSession(Long userId, String username, String token) {
        String sessionKey = SESSION_PREFIX + userId;
        try {
            String json = stringRedisTemplate.opsForValue().get(sessionKey);
            if (json != null) {
                return objectMapper.readValue(json, UserSession.class);
            }
        } catch (Exception e) {
            log.warn("Failed to read Redis session for userId={}, creating new", userId, e);
        }
        UserSession session = new UserSession(userId, username, System.currentTimeMillis(), token);
        try {
            String json = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(sessionKey, json, jwtConfig.getExpiration(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Failed to write Redis session for userId={}", userId, e);
        }
        return session;
    }
    
    /**
     * 从请求中提取Token
     * 
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtConfig.getHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtConfig.getPrefix())) {
            return bearerToken.substring(jwtConfig.getPrefix().length());
        }
        return null;
    }
    
    /**
     * 判断是否为公开路径（不需要认证）
     * 
     * @param requestPath 请求路径
     * @return true表示公开路径
     */
    private boolean isPublicPath(String requestPath) {
        return requestPath.contains("/swagger-ui")
            || requestPath.contains("/v3/api-docs")
            || requestPath.contains("/v1/users/register")
            || requestPath.contains("/v1/users/login");
    }
}
