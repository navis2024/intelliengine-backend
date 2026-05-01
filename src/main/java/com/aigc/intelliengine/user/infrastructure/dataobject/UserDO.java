package com.aigc.intelliengine.user.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据对象(User Data Object)
 * <p>
 * 对应数据库表: user_account
 * 位于COLA架构的基础设施层(Infrastructure Layer)
 * 直接与数据库表user_account映射，不含业务逻辑
 *
 * 数据库表结构：
 * - id: bigint unsigned, 主键，自增
 * - username: varchar(50), 唯一约束，非空
 * - password_hash: varchar(255), 非空 (存储加密后的密码)
 * - email: varchar(100), 唯一约束
 * - phone: varchar(20), 普通索引
 * - avatar_url: varchar(500) (头像URL)
 * - nickname: varchar(50) (昵称)
 * - user_type: varchar(20), 默认PERSONAL
 * - status: tinyint unsigned, 默认1 (0-禁用 1-正常)
 * - last_login_at: datetime (最后登录时间)
 * - created_at: datetime, 默认当前时间
 * - updated_at: datetime, 自动更新
 * - is_deleted: tinyint unsigned, 默认0 (逻辑删除标志)
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Data
@TableName("user_account")
public class UserDO {

    /**
     * 用户ID
     * 数据库类型: bigint unsigned, 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     * 数据库类型: varchar(50), 唯一约束，非空
     */
    private String username;

    /**
     * 密码哈希
     * 数据库类型: varchar(255), 非空
     * 存储内容: BCrypt加密后的密码哈希值
     */
    private String passwordHash;

    /**
     * 邮箱地址
     * 数据库类型: varchar(100), 唯一约束
     */
    private String email;

    /**
     * 手机号码
     * 数据库类型: varchar(20), 普通索引
     */
    private String phone;

    /**
     * 头像URL
     * 数据库类型: varchar(500)
     */
    private String avatarUrl;

    /**
     * 昵称
     * 数据库类型: varchar(50)
     */
    private String nickname;

    /**
     * 用户类型
     * 数据库类型: varchar(20), 默认PERSONAL
     * 值: PERSONAL(个人), ENTERPRISE(企业)
     */
    private String userType;

    /**
     * 账号状态
     * 数据库类型: tinyint unsigned, 默认1
     * 值定义：0-禁用, 1-正常
     */
    private Integer status;

    /**
     * 最后登录时间
     * 数据库类型: datetime
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     * 数据库类型: datetime, 默认当前时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 数据库类型: datetime, 自动更新
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志
     * 数据库类型: tinyint unsigned, 默认0
     * 值定义：0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
