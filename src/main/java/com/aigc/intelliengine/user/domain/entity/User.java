package com.aigc.intelliengine.user.domain.entity;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 用户领域实体
 * <p>
 * 位于COLA架构的领域层(Domain Layer)
 * 封装用户核心业务逻辑和数据，与数据存储无关
 * <p>
 * 设计原则：
 * 1. 纯Java类，不依赖任何框架注解（Spring、MyBatis等）
 * 2. 包含业务规则验证（如用户名格式、状态转换等）
 * 3. 通过充血模型封装业务行为
 * 4. 不包含password字段的对外暴露，但内部处理密码相关逻辑
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
public class User {

    // ==================== 常量定义 ====================

    /**
     * 用户状态：禁用
     * 账号被管理员禁用，无法登录
     */
    public static final int STATUS_DISABLED = 0;

    /**
     * 用户状态：正常
     * 账号状态正常，可以正常登录和使用
     */
    public static final int STATUS_NORMAL = 1;

    /**
     * 用户状态：未激活
     * 账号已注册但未激活（如未验证邮箱）
     */
    public static final int STATUS_INACTIVE = 2;

    /**
     * 未删除状态
     */
    public static final int NOT_DELETED = 0;

    /**
     * 已删除状态
     */
    public static final int DELETED = 1;

    /**
     * 用户名正则表达式：6-20位字母数字下划线
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 手机号正则表达式：中国大陆手机号
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    // ==================== 核心字段 ====================

    /**
     * 用户ID
     * 使用雪花算法生成的64位唯一标识
     */
    private String id;

    /**
     * 用户名
     * 6-20位字母数字下划线组合，全局唯一
     */
    private String username;

    // ==================== 联系信息 ====================

    /**
     * 邮箱地址
     * 用于找回密码、接收系统通知
     */
    private String email;

    /**
     * 手机号码
     * 用于短信通知、手机号登录
     */
    private String phone;

    // ==================== 个人资料 ====================

    /**
     * 头像URL
     * 存储头像图片的访问地址
     */
    private String avatar;

    // ==================== 状态管理 ====================

    /**
     * 账号状态
     * 0-禁用, 1-正常, 2-未激活
     */
    private Integer status;

    /**
     * 逻辑删除标志
     * 0-未删除, 1-已删除
     */
    private Integer deleted;

    // ==================== 审计字段 ====================

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 构造器 ====================

    /**
     * 默认构造器
     */
    public User() {
        this.status = STATUS_NORMAL;
        this.deleted = NOT_DELETED;
    }

    /**
     * 构造器 - 用于创建新用户
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     */
    public User(String username, String email, String phone) {
        this();
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    // ==================== Getter方法 ====================

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    // ==================== Setter方法（领域内部使用） ====================

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // ==================== 业务方法 - 状态检查 ====================

    /**
     * 检查用户是否处于正常状态
     *
     * @return true如果用户状态正常
     */
    public boolean isNormal() {
        return status != null && status == STATUS_NORMAL;
    }

    /**
     * 检查用户是否被禁用
     *
     * @return true如果用户被禁用
     */
    public boolean isDisabled() {
        return status != null && status == STATUS_DISABLED;
    }

    /**
     * 检查用户是否未激活
     *
     * @return true如果用户未激活
     */
    public boolean isInactive() {
        return status != null && status == STATUS_INACTIVE;
    }

    /**
     * 检查用户是否已被逻辑删除
     *
     * @return true如果用户已被删除
     */
    public boolean isDeleted() {
        return deleted != null && deleted == DELETED;
    }

    /**
     * 检查用户是否可以登录
     *
     * @return true如果用户状态允许登录（正常或未激活）
     */
    public boolean canLogin() {
        return !isDeleted() && (isNormal() || isInactive());
    }

    // ==================== 业务方法 - 状态转换 ====================

    /**
     * 激活用户账号
     * 将状态从未激活(2)转换为正常(1)
     *
     * @throws IllegalStateException 如果用户当前不是未激活状态
     */
    public void activate() {
        if (!isInactive()) {
            throw new IllegalStateException("只有未激活状态的账号才能被激活，当前状态: " + getStatusText());
        }
        this.status = STATUS_NORMAL;
    }

    /**
     * 禁用用户账号
     */
    public void disable() {
        this.status = STATUS_DISABLED;
    }

    /**
     * 启用用户账号
     */
    public void enable() {
        this.status = STATUS_NORMAL;
    }

    /**
     * 标记为删除
     */
    public void markDeleted() {
        this.deleted = DELETED;
    }

    // ==================== 业务方法 - 信息更新 ====================

    /**
     * 更新邮箱
     *
     * @param email 新邮箱
     * @throws IllegalArgumentException 如果邮箱格式不正确
     */
    public void updateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        this.email = email;
    }

    /**
     * 更新手机号
     *
     * @param phone 新手机号
     * @throws IllegalArgumentException 如果手机号格式不正确
     */
    public void updatePhone(String phone) {
        if (phone != null && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        this.phone = phone;
    }

    /**
     * 更新头像
     *
     * @param avatar 新头像URL
     */
    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }

    // ==================== 业务方法 - 验证 ====================

    /**
     * 验证用户名格式
     *
     * @param username 用户名
     * @return true如果格式正确
     */
    public static boolean validateUsernameFormat(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证邮箱格式
     *
     * @param email 邮箱
     * @return true如果格式正确
     */
    public static boolean validateEmailFormat(String email) {
        if (email == null || email.isEmpty()) {
            return true; // 邮箱可为空
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return true如果格式正确
     */
    public static boolean validatePhoneFormat(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // 手机号可为空
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证密码强度
     * 规则：6-20位
     *
     * @param password 密码
     * @return true如果符合要求
     */
    public static boolean validatePasswordFormat(String password) {
        return password != null && password.length() >= 6 && password.length() <= 20;
    }

    // ==================== 业务方法 - 工具 ====================

    /**
     * 获取状态描述文本
     *
     * @return 状态描述
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_DISABLED -> "禁用";
            case STATUS_NORMAL -> "正常";
            case STATUS_INACTIVE -> "未激活";
            default -> "未知";
        };
    }

    /**
     * 获取显示名称
     * 优先级：用户名 > 邮箱 > 手机号 > ID
     *
     * @return 用户显示名称
     */
    public String getDisplayName() {
        if (username != null && !username.isEmpty()) {
            return username;
        }
        if (email != null && !email.isEmpty()) {
            return email;
        }
        if (phone != null && !phone.isEmpty()) {
            return phone;
        }
        return id != null ? id : "未知用户";
    }

    // ==================== Object方法 ====================

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + (email != null ? "***" : null) + '\'' +
                ", phone='" + (phone != null ? "***" : null) + '\'' +
                ", status=" + status +
                ", deleted=" + deleted +
                '}';
    }
}
