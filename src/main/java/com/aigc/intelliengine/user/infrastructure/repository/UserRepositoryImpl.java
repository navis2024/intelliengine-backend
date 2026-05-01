package com.aigc.intelliengine.user.infrastructure.repository;

import com.aigc.intelliengine.user.domain.entity.User;
import com.aigc.intelliengine.user.domain.gateway.UserGateway;
import com.aigc.intelliengine.user.infrastructure.dataobject.UserDO;
import com.aigc.intelliengine.user.infrastructure.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
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
 * 设计原则：
 * 1. 实现领域层定义的仓储接口
 * 2. 封装技术细节（如MyBatis操作）
 * 3. 处理DO与Entity之间的转换
 * 4. 领域实体对外，数据对象对内
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
        userDO.setCreateTime(LocalDateTime.now());
        userDO.setUpdateTime(LocalDateTime.now());
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
        userDO.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(userDO);
        return toEntity(userDO);
    }

    @Override
    public boolean updatePassword(Long userId, String newPassword) {
        LambdaUpdateWrapper<UserDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserDO::getId, userId)
               .set(UserDO::getPassword, newPassword)
               .set(UserDO::getUpdateTime, LocalDateTime.now());
        return userMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean remove(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean delete(Long id) {
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
     * 将领域实体转换为数据对象
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
        userDO.setAvatar(user.getAvatar());
        userDO.setStatus(user.getStatus());
        userDO.setDeleted(user.getDeleted());
        userDO.setCreateTime(user.getCreateTime());
        userDO.setUpdateTime(user.getUpdateTime());
        return userDO;
    }

    /**
     * 将数据对象转换为领域实体
     */
    private User toEntity(UserDO userDO) {
        if (userDO == null) return null;
        User user = new User();
        user.setId(String.valueOf(userDO.getId()));
        user.setUsername(userDO.getUsername());
        user.setEmail(userDO.getEmail());
        user.setPhone(userDO.getPhone());
        user.setAvatar(userDO.getAvatar());
        user.setStatus(userDO.getStatus());
        user.setDeleted(userDO.getDeleted());
        user.setCreateTime(userDO.getCreateTime());
        user.setUpdateTime(userDO.getUpdateTime());
        return user;
    }
}
