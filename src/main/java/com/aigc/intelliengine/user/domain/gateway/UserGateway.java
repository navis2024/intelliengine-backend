package com.aigc.intelliengine.user.domain.gateway;

import com.aigc.intelliengine.user.domain.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户领域层仓储接口(User Gateway)
 * <p>
 * 位于COLA架构的领域层(Domain Layer)
 * 属于反转控制(Repositories)概念，封装数据持久化操作
 * <p>
 * 设计原则：
 * 1. 使用领域语言命名方法（如save、find、remove）
 * 2. 参数和返回值使用领域实体(User)，而非数据对象(DO)
 * 3. 将技术细节（如SQL、ORM）封装在实现中
 * 4. 领域层不关心数据如何存储，只关心业务逻辑
 * <p>
 * 实现位置：com.aigc.intelliengine.user.infrastructure.repository.UserRepositoryImpl
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 * @see User
 * @see com.aigc.intelliengine.user.infrastructure.repository.UserRepositoryImpl
 */
public interface UserGateway {

    /**
     * 保存用户
     * <p>
     * 插入新用户到数据库
     * 保存后用户对象会被赋值生成的ID
     *
     * @param user 待保存的用户实体（ID为空）
     * @return 保存后的用户实体（包含生成的ID）
     */
    User save(User user);

    /**
     * 根据ID查询用户
     * <p>
     * 查询指定ID的用户，不区分用户状态
     * 会过滤已逻辑删除的用户
     *
     * @param id 用户ID
     * @return 包含用户实体的Optional，如果不存在则返回空Optional
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查询用户
     * <p>
     * 用于登录验证、用户名唯一性检查
     * 会过滤已逻辑删除的用户
     *
     * @param username 用户名
     * @return 包含用户实体的Optional，如果不存在则返回空Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查询用户
     * <p>
     * 用于邮箱登录、邮箱唯一性检查
     * 会过滤已逻辑删除的用户
     *
     * @param email 邮箱地址
     * @return 包含用户实体的Optional，如果不存在则返回空Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查询用户
     * <p>
     * 用于手机号登录、手机号唯一性检查
     * 会过滤已逻辑删除的用户
     *
     * @param phone 手机号码
     * @return 包含用户实体的Optional，如果不存在则返回空Optional
     */
    Optional<User> findByPhone(String phone);

    /**
     * 更新用户信息
     * <p>
     * 根据用户ID更新用户信息
     * 不更新密码字段（密码更新使用单独方法）
     * 不更新用户名（用户名不允许修改）
     *
     * @param user 待更新的用户实体（必须包含ID）
     * @return 更新后的用户实体
     */
    User update(User user);

    /**
     * 更新用户密码
     * <p>
     * 单独提供密码更新方法，安全要求更高
     *
     * @param userId      用户ID
     * @param newPassword 新密码（已加密）
     * @return true如果更新成功
     */
    boolean updatePassword(Long userId, String newPassword);

    /**
     * 逻辑删除用户
     * <p>
     * 不是物理删除，而是将deleted字段置为1
     * 删除后用户无法登录，但数据仍保留在数据库中
     *
     * @param id 用户ID
     * @return true如果删除成功
     */
    boolean remove(Long id);

    /**
     * 物理删除用户
     * <p>
     * 从数据库中永久删除用户记录
     * 应谨慎使用，通常只在数据清理时使用
     *
     * @param id 用户ID
     * @return true如果删除成功
     */
    boolean delete(Long id);

    /**
     * 检查用户名是否存在
     * <p>
     * 用于注册时的用户名唯一性校验
     *
     * @param username 用户名
     * @return true如果已存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已被使用
     * <p>
     * 用于注册和修改邮箱时的唯一性校验
     *
     * @param email 邮箱地址
     * @return true如果已被使用
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否已被使用
     * <p>
     * 用于注册和修改手机号时的唯一性校验
     *
     * @param phone 手机号码
     * @return true如果已被使用
     */
    boolean existsByPhone(String phone);
}
