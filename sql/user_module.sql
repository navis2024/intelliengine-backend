-- ============================================================================
-- 智擎(IntelliEngine)用户模块 - 数据库表设计
-- 表名: t_user
-- 描述: 用户基础信息表，存储用户账号、密码、联系方式等核心数据
-- 架构: COLA 4.0
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 创建用户表
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user` (
    -- 主键字段
    `id` BIGINT NOT NULL COMMENT '用户ID，使用雪花算法生成的唯一标识',
    
    -- 核心账号信息
    `username` VARCHAR(20) NOT NULL COMMENT '用户名，6-20位字母数字下划线，全局唯一',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码，使用BCrypt等强哈希算法存储',
    
    -- 联系方式
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址，用于通知和找回密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号，用于短信通知和登录',
    
    -- 个人资料
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL，存储头像图片的访问地址',
    
    -- 状态管理
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '账号状态：0-禁用 1-正常 2-未激活',
    
    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 逻辑删除
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除标志：0-未删除 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`id`),
    
    -- 唯一索引
    UNIQUE KEY `uk_username` (`username`) COMMENT '用户名唯一索引，确保用户名不重复',
    UNIQUE KEY `uk_email` (`email`) COMMENT '邮箱唯一索引，确保邮箱不重复（允许NULL但非重复值）',
    UNIQUE KEY `uk_phone` (`phone`) COMMENT '手机号唯一索引，确保手机号不重复（允许NULL但非重复值）',
    
    -- 普通索引
    KEY `idx_status` (`status`) COMMENT '状态索引，方便按状态筛选用户',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引，方便按时间排序和筛选'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- ----------------------------------------------------------------------------
-- 索引优化说明
-- ----------------------------------------------------------------------------
-- 1. uk_username: 用户名登录查询优化
-- 2. uk_email: 邮箱登录查询优化
-- 3. uk_phone: 手机号登录查询优化
-- 4. idx_status: 按状态筛选用户（如只查询正常状态用户）
-- 5. idx_create_time: 按注册时间排序或筛选

-- ----------------------------------------------------------------------------
-- 初始化测试数据（可选）
-- ----------------------------------------------------------------------------
-- 注意：以下数据仅用于开发和测试环境，生产环境请删除或修改
-- 密码示例: 'password123' 的BCrypt加密结果
/*
INSERT INTO `t_user` (`id`, `username`, `password`, `email`, `phone`, `avatar`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1000000000000000001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'admin@intelliengine.com', '13800138000', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1, NOW(), NOW(), 0),
(1000000000000000002, 'testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'test@example.com', '13800138001', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test', 1, NOW(), NOW(), 0);
*/
