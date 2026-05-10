package com.aigc.intelliengine.user;

import com.aigc.intelliengine.user.model.entity.UserAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Select("SELECT * FROM user_account WHERE username = #{username} AND is_deleted = 0 LIMIT 1")
    UserAccount selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM user_account WHERE email = #{email} AND is_deleted = 0 LIMIT 1")
    UserAccount selectByEmail(@Param("email") String email);

    @Select("SELECT * FROM user_account WHERE phone = #{phone} AND is_deleted = 0 LIMIT 1")
    UserAccount selectByPhone(@Param("phone") String phone);

    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE username = #{username} AND is_deleted = 0)")
    boolean existsByUsername(@Param("username") String username);

    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE email = #{email} AND is_deleted = 0)")
    boolean existsByEmail(@Param("email") String email);

    @Select("SELECT EXISTS(SELECT 1 FROM user_account WHERE phone = #{phone} AND is_deleted = 0)")
    boolean existsByPhone(@Param("phone") String phone);

    @Select("SELECT password_hash FROM user_account WHERE username = #{username} AND is_deleted = 0")
    String selectPasswordByUsername(@Param("username") String username);

    @Update("UPDATE user_account SET last_login_at = NOW(), updated_at = NOW() WHERE id = #{userId}")
    int updateLastLoginTime(@Param("userId") Long userId);
}
