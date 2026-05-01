package com.aigc.intelliengine.user.infrastructure.mapper;

import com.aigc.intelliengine.user.infrastructure.dataobject.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问映射器(User Mapper)
 * <p>
 * 位于COLA架构的基础设施层(Infrastructure Layer)
 * 使用MyBatis Plus进行数据库CRUD操作
 * <p>
 * 设计原则：
 * 1. 继承BaseMapper获得基础CRUD能力
 * 2. 使用@Mapper注解让Spring扫描
 * 3. 自定义查询使用注解或XML配置
 * 4. 方法命名清晰，与业务无关
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
     * 使用@Select注解直接编写SQL
     * 逻辑删除字段会自动过滤
     *
     * @param username 用户名
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0 LIMIT 1")
    UserDO selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted = 0 LIMIT 1")
    UserDO selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号码
     * @return 用户数据对象，如果不存在返回null
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted = 0 LIMIT 1")
    UserDO selectByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否存在
     * <p>
     * 使用EXISTS查询，比COUNT(*)更高效
     *
     * @param username 用户名
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM t_user WHERE username = #{username} AND deleted = 0)")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱地址
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM t_user WHERE email = #{email} AND deleted = 0)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号码
     * @return true如果存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM t_user WHERE phone = #{phone} AND deleted = 0)")
    boolean existsByPhone(@Param("phone") String phone);
}
