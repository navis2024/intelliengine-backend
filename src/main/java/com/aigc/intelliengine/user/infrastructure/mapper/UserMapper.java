package com.aigc.intelliengine.user.infrastructure.mapper;

import com.aigc.intelliengine.user.infrastructure.dataobject.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问映射器(User Mapper)
 * <p>
 * 对应数据库表: user_account
 * 使用MyBatis Plus进行数据库CRUD操作
 * <p>
 * 数据库索引:
 * - PRIMARY KEY: id
 * - UNIQUE KEY: username
 * - UNIQUE KEY: email
 * - KEY: phone
 * - KEY: status
 * - KEY: created_at
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 * @see UserDO
 * @see BaseMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 根据用户名查询用户
     * <p>
     * SQL优化: 使用索引 idx_username
     * 过滤条件: is_deleted = 0
     *
     * @param username 用户名
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM user_account WHERE username = #{username} AND is_deleted = 0 LIMIT 1")
    UserDO selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     * <p>
     * SQL优化: 使用索引 uk_email
     *
     * @param email 邮箱地址
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM user_account WHERE email = #{email} AND is_deleted = 0 LIMIT 1")
    UserDO selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     * <p>
     * SQL优化: 使用索引 idx_phone
     *
     * @param phone 手机号码
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM user_account WHERE phone = #{phone} AND is_deleted = 0 LIMIT 1")
    UserDO selectByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否存在
     * <p>
     * 使用EXISTS查询比COUNT(*)更高效
     * 索引优化: 使用uk_username唯一索引
     *
     * @param username 用户名
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE username = #{username} AND is_deleted = 0)")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     * <p>
     * 索引优化: 使用uk_email唯一索引
     *
     * @param email 邮箱地址
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE email = #{email} AND is_deleted = 0)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     * <p>
     * 索引优化: 使用idx_phone普通索引
     *
     * @param phone 手机号码
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE phone = #{phone} AND is_deleted = 0)")
    boolean existsByPhone(@Param("phone") String phone);

    /**
     * 根据ID查询密码哈希
     * <p>
     * 用于登录时密码验证
     * 只返回password_hash字段，减少数据传输
     *
     * @param id 用户ID
     * @return 密码哈希字符串
     */
    @Select("SELECT password_hash FROM user_account WHERE id = #{id} AND is_deleted = 0")
    String selectPasswordById(@Param("id") Long id);

    /**
     * 根据用户名查询密码哈希
     * <p>
     * 用于登录时密码验证
     *
     * @param username 用户名
     * @return 密码哈希字符串
     */
    @Select("SELECT password_hash FROM user_account WHERE username = #{username} AND is_deleted = 0")
    String selectPasswordByUsername(@Param("username") String username);

    /**
     * 更新最后登录时间
     * <p>
     * 在用户登录成功后调用
     *
     * @param userId 用户ID
     * @return 影响的行数
     */
    @Select("UPDATE user_account SET last_login_at = NOW(), updated_at = NOW() WHERE id = #{userId}")
    int updateLastLoginTime(@Param("userId") Long userId);
}
