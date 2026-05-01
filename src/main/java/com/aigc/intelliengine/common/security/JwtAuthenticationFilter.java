package com.aigc.intelliengine.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT Token并设置用户认证信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 获取请求路径
        String requestPath = request.getRequestURI();
        
        // 跳过不需要认证的路径
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取Token
        String token = extractTokenFromRequest(request);
        
        // 验证Token
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 从Token中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (userId != null) {
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        new JwtUserDetails(userId, username), 
                        null, 
                        new ArrayList<>()
                    );
                
                // 设置认证信息到Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 将userId设置到request属性中，方便后续使用
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                
                log.debug("JWT认证成功 - 用户: {}, 路径: {}", username, requestPath);
            }
        } else {
            log.debug("JWT认证失败或Token缺失 - 路径: {}", requestPath);
        }
        
        filterChain.doFilter(request, response);
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
            || requestPath.contains("/api/v1/users/register")
            || requestPath.contains("/api/v1/users/login");
    }
}
