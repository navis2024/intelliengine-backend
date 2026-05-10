package com.aigc.intelliengine.user.app.service;

import com.aigc.intelliengine.user.UserAccountService;
import com.aigc.intelliengine.user.UserAccountMapper;
import com.aigc.intelliengine.user.model.entity.UserAccount;
import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.JwtUtil;
import com.aigc.intelliengine.user.model.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {

    @Mock private UserAccountMapper userAccountMapper;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private UserAccountService userAccountService;

    @Test
    void testUserEntityFields() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        user.setStatus(UserAccount.STATUS_NORMAL);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isNormal());
    }

    @Test
    void testUserStatusCheck() {
        UserAccount user = new UserAccount();
        user.setStatus(UserAccount.STATUS_NORMAL);
        assertTrue(user.isNormal());

        user.setStatus(UserAccount.STATUS_DISABLED);
        assertTrue(user.isDisabled());
    }

    @Test
    void testValidateUsername() {
        assertTrue(UserAccount.validateUsernameFormat("valid_user1"));
        assertFalse(UserAccount.validateUsernameFormat("ab"));
        assertFalse(UserAccount.validateUsernameFormat(""));
        assertFalse(UserAccount.validateUsernameFormat(null));
    }

    @Test
    void testValidateEmail() {
        assertTrue(UserAccount.validateEmailFormat("test@example.com"));
        assertTrue(UserAccount.validateEmailFormat(""));
        assertTrue(UserAccount.validateEmailFormat(null));
        assertFalse(UserAccount.validateEmailFormat("notanemail"));
    }

    @Test
    void testRegisterDuplicateUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing_user");
        req.setPassword("test123456");
        when(userAccountMapper.existsByUsername("existing_user")).thenReturn(true);
        assertThrows(BusinessException.class, () -> userAccountService.register(req));
    }
}
