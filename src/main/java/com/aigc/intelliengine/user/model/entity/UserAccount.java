package com.aigc.intelliengine.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 用户账户实体 — 直接映射 user_account 表
 * 合并了原 COLA 架构的 UserDO (数据对象) + User (领域实体)
 */
@Data
@TableName("user_account")
public class UserAccount {

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_INACTIVE = 2;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private String phone;
    private String avatarUrl;
    private String nickname;
    private String userType;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    public boolean isNormal() { return status != null && status == STATUS_NORMAL; }
    public boolean isDisabled() { return status != null && status == STATUS_DISABLED; }
    public boolean canLogin() { return isDeleted == null || isDeleted == 0 && (isNormal() || (status != null && status == STATUS_INACTIVE)); }

    public static boolean validateUsernameFormat(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    public static boolean validateEmailFormat(String email) {
        return email == null || email.isEmpty() || EMAIL_PATTERN.matcher(email).matches();
    }
    public static boolean validatePhoneFormat(String phone) {
        return phone == null || phone.isEmpty() || PHONE_PATTERN.matcher(phone).matches();
    }
    public static boolean validatePasswordFormat(String password) {
        return password != null && password.length() >= 6 && password.length() <= 20;
    }
}
