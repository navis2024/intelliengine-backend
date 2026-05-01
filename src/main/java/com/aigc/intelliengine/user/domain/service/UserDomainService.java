package com.aigc.intelliengine.user.domain.service;

import com.aigc.intelliengine.user.domain.entity.User;
import com.aigc.intelliengine.user.domain.gateway.UserGateway;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 用户领域服务
 * <p>
 * 位于COLA架构的领域层(Domain Layer)
 * 处理复杂的业务逻辑，特别是需要多个领域实体协同或需要使用传输门(gateway)的逻辑
 * <p>
 * 设计原则：
 * 1. 不依赖Spring框架，通过构造函数注入依赖
 * 2. 处理跨实体的业务规则（如唯一性校验）
 * 3. 处理需要业务判断的操作（如状态变更检查）
 * 4. 领域实体负责单实体的行为，领域服务负责跨实体的行为
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
public class UserDomainService {

    /**
     * 用户传输门(gateway)
     * 通过构造函数注入，不依赖Spring框架
     */
    private final UserGateway userGateway;

    /**
     * 构造函数 - 注入依赖
     *
     * @param userGateway 用户传输门
     */
    public UserDomainService(UserGateway userGateway) {
        this.userGateway = Objects.requireNonNull(userGateway, "UserGateway不能为空");
    }

    /**
     * 用户注册业务逻辑
     * <p>
     * 执行注册前的完整校验：
     * 1. 校验用户名格式
     * 2. 校验用户名唯一性
     * 3. 校验邮箱格式（如果提供）
     * 4. 校验邮箱唯一性（如果提供）
     * 5. 校验手机号格式（如果提供）
     * 6. 校验手机号唯一性（如果提供）
     *
     * @param user 待注册的用户实体
     * @throws IllegalArgumentException 如果校验失败
     * @throws IllegalStateException    如果唯一性校验失败
     */
    public void validateRegister(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        // 1. 校验用户名
        String username = user.getUsername();
        if (!User.validateUsernameFormat(username)) {
            throw new IllegalArgumentException("用户名格式不正确，要求6-20位字母数字下划线");
        }

        // 2. 校验用户名唯一性
        if (userGateway.existsByUsername(username)) {
            throw new IllegalStateException("用户名已被使用");
        }

        // 3. 校验邮箱格式和唯一性
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            if (!User.validateEmailFormat(email)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            if (userGateway.existsByEmail(email)) {
                throw new IllegalStateException("邮箱已被使用");
            }
        }

        // 4. 校验手机号格式和唯一性
        String phone = user.getPhone();
        if (phone != null && !phone.isEmpty()) {
            if (!User.validatePhoneFormat(phone)) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            if (userGateway.existsByPhone(phone)) {
                throw new IllegalStateException("手机号已被使用");
            }
        }
    }

    /**
     * 创建用户 - 带有默认值设置
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @return 新创建的用户实体
     */
    public User createUser(String username, String email, String phone) {
        User user = new User(username, email, phone);
        user.setStatus(User.STATUS_NORMAL);
        user.setDeleted(User.NOT_DELETED);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 用户状态变更业务逻辑
     * <p>
     * 执行状态变更前的完整校查：
     * 1. 检查用户是否存在
     * 2. 检查目标状态是否与当前状态相同
     * 3. 执行状态转换
     *
     * @param userId     用户ID
     * @param newStatus  目标状态
     * @param operatorId 操作人ID（用于审计）
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     * @throws IllegalStateException    如果状态转换不允许
     */
    public User changeStatus(String userId, int newStatus, String operatorId) {
        // 1. 查找用户
        User user = userGateway.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 检查是否已删除
        if (user.isDeleted()) {
            throw new IllegalStateException("已删除的用户不能修改状态");
        }

        // 3. 检查目标状态是否有效
        if (newStatus != User.STATUS_DISABLED &&
            newStatus != User.STATUS_NORMAL &&
            newStatus != User.STATUS_INACTIVE) {
            throw new IllegalArgumentException("无效的状态值: " + newStatus);
        }

        // 4. 检查是否需要变更
        if (Objects.equals(user.getStatus(), newStatus)) {
            return user; // 状态相同，无需更新
        }

        // 5. 执行状态转换
        switch (newStatus) {
            case User.STATUS_DISABLED:
                user.disable();
                break;
            case User.STATUS_NORMAL:
                user.enable();
                break;
            case User.STATUS_INACTIVE:
                // 通常不允许直接设置为未激活，需要特殊处理
                throw new IllegalStateException("不允许直接将用户设置为未激活状态");
        }

        user.setUpdateTime(LocalDateTime.now());
        return userGateway.update(user);
    }

    /**
     * 禁用用户
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     * @return 更新后的用户实体
     */
    public User disableUser(String userId, String operatorId) {
        return changeStatus(userId, User.STATUS_DISABLED, operatorId);
    }

    /**
     * 启用用户
     *
     * @param userId     用户ID
     * @param operatorId 操作人ID
     * @return 更新后的用户实体
     */
    public User enableUser(String userId, String operatorId) {
        return changeStatus(userId, User.STATUS_NORMAL, operatorId);
    }

    /**
     * 更新用户信息业务逻辑
     * <p>
     * 执行更新前的完整校验：
     * 1. 检查用户是否存在
     * 2. 检查邮箱唯一性（如果更新邮箱）
     * 3. 检查手机号唯一性（如果更新手机号）
     * 4. 执行更新
     *
     * @param userId   用户ID
     * @param newEmail 新邮箱（可为空表示不更新）
     * @param newPhone 新手机号（可为空表示不更新）
     * @param newAvatar 新头像（可为空表示不更新）
     * @return 更新后的用户实体
     * @throws IllegalArgumentException 如果用户不存在或参数不合法
     */
    public User updateUserInfo(String userId, String newEmail, String newPhone, String newAvatar) {
        // 1. 查找用户
        User user = userGateway.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 检查是否已删除
        if (user.isDeleted()) {
            throw new IllegalStateException("已删除的用户不能修改信息");
        }

        // 3. 校验并更新邮箱
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (!User.validateEmailFormat(newEmail)) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
            if (userGateway.existsByEmail(newEmail)) {
                throw new IllegalStateException("邮箱已被其他用户使用");
            }
            user.updateEmail(newEmail);
        }

        // 4. 校验并更新手机号
        if (newPhone != null && !newPhone.equals(user.getPhone())) {
            if (!User.validatePhoneFormat(newPhone)) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            if (userGateway.existsByPhone(newPhone)) {
                throw new IllegalStateException("手机号已被其他用户使用");
            }
            user.updatePhone(newPhone);
        }

        // 5. 更新头像
        if (newAvatar != null) {
            user.updateAvatar(newAvatar);
        }

        user.setUpdateTime(LocalDateTime.now());
        return userGateway.update(user);
    }

    /**
     * 根据登录账号查找用户
     * <p>
     * 支持用户名、邮箱、手机号登录
     *
     * @param account 登录账号
     * @return 用户实体
     * @throws IllegalArgumentException 如果用户不存在
     */
    public User findByLoginAccount(String account) {
        if (account == null || account.isEmpty()) {
            throw new IllegalArgumentException("登录账号不能为空");
        }

        // 1. 尝试按用户名查找
        return userGateway.findByUsername(account)
                // 2. 尝试按邮箱查找
                .or(() -> userGateway.findByEmail(account))
                // 3. 尝试按手机号查找
                .or(() -> userGateway.findByPhone(account))
                // 4. 都没找到则抛异常
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 验证登录状态
     * <p>
     * 检查用户是否可以登录
     *
     * @param user 用户实体
     * @throws IllegalStateException 如果用户不能登录
     */
    public void validateLoginStatus(User user) {
        if (user.isDeleted()) {
            throw new IllegalStateException("账号已注销");
        }
        if (user.isDisabled()) {
            throw new IllegalStateException("账号已被禁用，请联系管理员");
        }
        if (!user.canLogin()) {
            throw new IllegalStateException("账号状态异常，无法登录");
        }
    }

    /**
     * 物理删除用户检查
     * <p>
     * 在物理删除前执行必要的检查
     *
     * @param userId 用户ID
     * @return 被删除的用户实体
     * @throws IllegalArgumentException 如果用户不存在
     * @throws IllegalStateException    如果用户状态不允许物理删除
     */
    public User validatePhysicalDelete(String userId) {
        User user = userGateway.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 可以添加更多业务规则，如：管理员不能被删除、有未完成业务的用户不能被删除等
        return user;
    }
}
