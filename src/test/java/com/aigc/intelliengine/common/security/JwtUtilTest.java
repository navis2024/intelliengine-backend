package com.aigc.intelliengine.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("TestSecretKeyForJWTGeneration2024IntelliEngineNavis2024");
        jwtConfig.setExpiration(604800000L);
        
        jwtUtil = new JwtUtil(jwtConfig);
        jwtUtil.init();
    }

    @Test
    void testGenerateToken() {
        // 执行
        String token = jwtUtil.generateToken(1L, "testuser");

        // 验证
        assertNotNull(token);
        assertTrue(token.contains("."));
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length); // Header.Payload.Signature
    }

    @Test
    void testValidateToken_Valid() {
        // 准备
        String token = jwtUtil.generateToken(1L, "testuser");

        // 执行和验证
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testGetUserIdFromToken() {
        // 准备
        String token = jwtUtil.generateToken(1L, "testuser");

        // 执行
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 验证
        assertEquals(1L, userId);
    }

    @Test
    void testGetUsernameFromToken() {
        // 准备
        String token = jwtUtil.generateToken(1L, "testuser");

        // 执行
        String username = jwtUtil.getUsernameFromToken(token);

        // 验证
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken_Invalid() {
        // 执行和验证
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}
