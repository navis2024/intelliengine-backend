package com.aigc.intelliengine.user.app.service;

import com.aigc.intelliengine.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户应用服务测试类
 */
class UserAppServiceTest {

    @BeforeEach
    void setUp() {
        // 测试准备
    }

    @Test
    void testUserEntity() {
        // 测试领域实体
        User user = new User();
        user.setId("1");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(1);

        assertNotNull(user);
        assertEquals("1", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    void testUserStatus() {
        User user = new User();
        user.setStatus(1);
        
        assertTrue(user.isNormal());
        assertFalse(user.isDisabled());
    }
}
