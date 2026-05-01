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
 * 位于COLA架构的基础设施层(Infrastructure Layer)
 * 直接与数据库表t_user映射，不含业务逻辑
 * <p>
 * 设计原则：
 * 1. 使用MyBatis Plus注解进行ORM映射
 * 2. 包含所有数据库字段，不含计算字段
 * 3. 使用Lombok减少模板代码
 * 4. 符合MyBatis Plus的逻辑删除规范
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 * @see com.baomidou.mybatisplus.annotation.TableName
 */
@Data
@TableName("t_user")
public class UserDO {

    // ==================== 核心字段 ====================

    /**
     * 用户ID
     * <p>
     * 使用雪花算法生成的64位整数ID
     * IdType.ASSIGN_ID: 由MyBatis Plus的IdWorker生成雪花ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     * <p>
     * 数据库约束：VARCHAR(20)，唯一约束，非空
     * 格式要求：6-20位字母数字下划线组合
     */
    private String username;

    /**
     * 密码
     * <p>
     * 数据库约束：VARCHAR(255)，非空
     * 存储内容：BCrypt加密后的密码哈希值
     * 注意：绝不存储明文密码
     */
    private String password;

    // ==================== 联系信息 ====================

    /**
     * 邮箱地址
     * <p>
     * 数据库约束：VARCHAR(100)，唯一约束，可为空
     * 用途：找回密码、系统通知、登录
     */
    private String email;

    /**
     * 手机号码
     * <p>
     * 数据库约束：VARCHAR(20)，唯一约束，可为空
     * 用途：短信通知、手机号登录
     */
    private String phone;

    // ==================== 个人资料 ====================

    /**
     * 头像URL
     * <p>
     * 数据库约束：VARCHAR(500)，可为空
     * 存储内容：头像图片的完整HTTP(S)URL或对象存储路径
     */
    private String avatar;

    // ==================== 状态管理 ====================

    /**
     * 账号状态
     * <p>
     * 数据库约束：TINYINT，非空，默认值1
     * 值定义：0-禁用 1-正常 2-未激活
     */
    private Integer status;

    // ==================== 审计字段 ====================

    /**
     * 创建时间
     * <p>
     * 数据库约束：DATETIME，非空，默认当前时间
     * 自动填充：由数据库在插入时自动设置
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>
     * 数据库约束：DATETIME，非空，默认当前时间，更新时自动更新
     * 自动填充：由数据库在更新时自动设置
     */
    private LocalDateTime updateTime;

    // ==================== 逻辑删除 ====================

    /**
     * 逻辑删除标志
     * <p>
     * 数据库约束：TINYINT，非空，默认值0
     * 值定义：0-未删除 1-已删除
     * <p>
     * 注解说明：
     * @TableLogic 标记该字段为逻辑删除字段
     * 当调用MyBatis Plus的deleteById等方法时，
     * 会自动将该字段更新为1，而不是真正删除记录
     * 查询时会自动过滤deleted=1的记录
     */
    @TableLogic
    private Integer deleted;
}
