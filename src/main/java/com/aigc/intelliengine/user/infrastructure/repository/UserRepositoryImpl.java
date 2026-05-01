package com.aigc.intelliengine.user.infrastructure.repository;

import com.aigc.intelliengine.user.domain.entity.User;
import com.aigc.intelliengine.user.domain.gateway.UserGateway;
import com.aigc.intelliengine.user.infrastructure.dataobject.UserDO;
import com.aigc.intelliengine.user.infrastructure.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 用户仓储实现(User Repository Implementation)
 * <p>
 * 位于COLA架构的基础设施层(Infrastructure Layer)
 * 实现领域层定义的UserGateway接口
 * 负责领域实体(User)与数据对象(UserDO)之间的转换
 * <p>
 * 数据库表: user_account
 * 字段映射:
 * - User.id (String) ↔ UserDO.id (Long)
 * - User.username ↔ UserDO.username
 * - User.email ↔ UserDO.email
 * - User.phone ↔ UserDO.phone
 * - User.avatar ↔ UserDO.avatarUrl
 * - User.status ↔ UserDO.status
 * - User.deleted ↔ UserDO.isDeleted
 * - User.createTime ↔ UserDO.createdAt
 * - User.updateTime ↔ UserDO.updatedAt
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 * @see UserGateway
 * @see User
 * @see UserDO
 */
@Repository
public class UserRepositoryImpl implements UserGateway {

    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = Objects.requireNonNull(userMapper, "UserMapper不能为空");
    }

    @Override
    public User save(User user) {
        UserDO userDO = toDataObject(user);
        userMapper.insert(userDO);
        return toEntity(userDO);
    }

    @Override
    public Optional<User> findById(Long id) {
        UserDO userDO = userMapper.selectById(id);
        return Optional.ofNullable(toEntity(userDO));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        UserDO userDO = userMapper.selectByUsername(username);
        return Optional.ofNullable(toEntity(userDO));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserDO userDO = userMapper.selectByEmail(email);
        return Optional.ofNullable(toEntity(userDO));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        UserDO userDO = userMapper.selectByPhone(phone);
        return Optional.ofNullable(toEntity(userDO));
    }

    @Override
    public User update(User user) {
        UserDO userDO = toDataObject(user);
        userMapper.updateById(userDO);
        return toEntity(userDO);
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getId, userId)
               .set(UserDO::getPasswordHash, newPassword);
        return userMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean remove(Long id) {
        // MyBatis Plus逻辑删除
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean delete(Long id) {
        // 物理删除
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.existsByPhone(phone);
    }

    /**
     * 根据用户名查询密码哈希
     *
     * @param username 用户名
     * @return 密码哈希
     */
    public String getPasswordByUsername(String username) {
        return userMapper.selectPasswordByUsername(username);
    }

    /**
     * 更新最后登录时间
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean updateLastLoginTime(Long userId) {
        return userMapper.updateLastLoginTime(userId) > 0;
    }

    /**
     * 将领域实体转换为数据对象
     * <p>
     * 字段映射说明:
     * - User.id (String) → UserDO.id (Long)
     * - User.avatar → UserDO.avatarUrl
     * - User.deleted → UserDO.isDeleted
     * - User.createTime → UserDO.createdAt
     * - User.updateTime → UserDO.updatedAt
     */
    private UserDO toDataObject(User user) {
        if (user == null) return null;
        UserDO userDO = new UserDO();
        if (user.getId() != null) {
            userDO.setId(Long.valueOf(user.getId()));
        }
        userDO.setUsername(user.getUsername());
        userDO.setEmail(user.getEmail());
        userDO.setPhone(user.getPhone());
        userDO.setAvatarUrl(user.getAvatar());
        userDO.setStatus(user.getStatus());
        userDO.setIsDeleted(user.getDeleted());
        userDO.setCreatedAt(user.getCreateTime());
        userDO.setUpdatedAt(user.getUpdateTime());
        return userDO;
    }

    /**
     * 将数据对象转换为领域实体
     * <p>
     * 字段映射说明:
     * - UserDO.id (Long) → User.id (String)
     * - UserDO.avatarUrl → User.avatar
     * - UserDO.isDeleted → User.deleted
     * - UserDO.createdAt → User.createTime
     * - UserDO.updatedAt → User.updateTime
     */
    private User toEntity(UserDO userDO) {
        if (userDO == null) return null;
        User user = new User();
        user.setId(String.valueOf(userDO.getId()));
        user.setUsername(userDO.getUsername());
        user.setEmail(userDO.getEmail());
        user.setPhone(userDO.getPhone());
        user.setAvatar(userDO.getAvatarUrl());
        user.setStatus(userDO.getStatus());
        user.setDeleted(userDO.getIsDeleted());
        user.setCreateTime(userDO.getCreatedAt());
        user.setUpdateTime(userDO.getUpdatedAt());
        return user;
    }
}
