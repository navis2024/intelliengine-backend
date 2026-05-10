package com.aigc.intelliengine.common.security;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserContextHolder {

    private static final ThreadLocal<UserSession> USER_CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void set(UserSession session) {
        USER_CONTEXT.set(session);
    }

    public static UserSession get() {
        return USER_CONTEXT.get();
    }

    public static Long getCurrentUserId() {
        UserSession session = USER_CONTEXT.get();
        return session != null ? session.getUserId() : null;
    }

    public static String getCurrentUsername() {
        UserSession session = USER_CONTEXT.get();
        return session != null ? session.getUsername() : null;
    }

    public static void clear() {
        USER_CONTEXT.remove();
    }
}
