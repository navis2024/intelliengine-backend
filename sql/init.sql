-- =====================================================
-- 智擎 IntelliEngine - 完整数据库初始化脚本
-- 版本: v1.1.0
-- 日期: 2026-05-06
-- 数据库: intelliengine
-- =====================================================

-- =====================================================
-- 1. 用户模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `user_type` VARCHAR(20) DEFAULT 'PERSONAL' COMMENT '用户类型: PERSONAL/ENTERPRISE',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

CREATE TABLE IF NOT EXISTS `user_auth` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `auth_type` VARCHAR(20) NOT NULL DEFAULT 'PASSWORD' COMMENT '认证类型',
    `auth_key` VARCHAR(100) DEFAULT NULL COMMENT '认证键',
    `auth_secret` VARCHAR(255) DEFAULT NULL COMMENT '认证密钥',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- =====================================================
-- 2. 项目管理模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `project_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_code` VARCHAR(50) NOT NULL COMMENT '项目编码',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` TEXT DEFAULT NULL COMMENT '项目描述',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `owner_id` BIGINT NOT NULL COMMENT '所有者ID',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ARCHIVED/DELETED',
    `visibility` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/PUBLIC',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_code` (`project_code`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目信息表';

CREATE TABLE IF NOT EXISTS `project_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT 'OWNER/ADMIN/EDITOR/VIEWER/MEMBER',
    `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- =====================================================
-- 3. 资产管理模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `asset_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_code` VARCHAR(50) NOT NULL COMMENT '资产编码',
    `name` VARCHAR(200) NOT NULL COMMENT '资产名称',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: VIDEO/IMAGE/AUDIO/TEMPLATE/OTHER',
    `owner_type` VARCHAR(20) DEFAULT 'USER' COMMENT '所属类型: USER/PROJECT/PLATFORM',
    `owner_id` BIGINT DEFAULT NULL COMMENT '所有者ID',
    `source_asset_id` BIGINT DEFAULT NULL COMMENT '源资产ID',
    `source_version` INT DEFAULT NULL COMMENT '源版本号',
    `version` INT DEFAULT 1 COMMENT '当前版本号',
    `is_latest` TINYINT DEFAULT 1 COMMENT '是否最新版本',
    `commit_message` VARCHAR(500) DEFAULT NULL COMMENT '提交信息',
    `committed_by` BIGINT DEFAULT NULL,
    `committed_at` DATETIME DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'UPLOADING' COMMENT '状态',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `file_format` VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    `duration` INT DEFAULT NULL COMMENT '时长(秒)',
    `created_by` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    KEY `idx_owner` (`owner_type`, `owner_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产信息表';

CREATE TABLE IF NOT EXISTS `asset_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_id` BIGINT NOT NULL,
    `version_number` INT NOT NULL COMMENT '版本号',
    `change_log` TEXT DEFAULT NULL COMMENT '变更日志',
    `created_by` BIGINT DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产版本表';

-- =====================================================
-- 4. 模板市场模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `market_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_id` BIGINT NOT NULL COMMENT '关联资产ID',
    `title` VARCHAR(200) NOT NULL COMMENT '模板标题',
    `description` TEXT DEFAULT NULL,
    `category_id` BIGINT DEFAULT NULL,
    `price` DECIMAL(10,2) DEFAULT 0.00,
    `original_price` DECIMAL(10,2) DEFAULT NULL,
    `currency` VARCHAR(10) DEFAULT 'CNY',
    `sales_count` INT DEFAULT 0,
    `view_count` INT DEFAULT 0,
    `rating` DECIMAL(3,2) DEFAULT 0.00,
    `status` VARCHAR(20) DEFAULT 'PUBLISHED',
    `created_by` BIGINT DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板市场表';

CREATE TABLE IF NOT EXISTS `market_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `total_amount` DECIMAL(10,2) DEFAULT 0.00,
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00,
    `pay_amount` DECIMAL(10,2) DEFAULT 0.00,
    `currency` VARCHAR(10) DEFAULT 'CNY',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    `pay_time` DATETIME DEFAULT NULL,
    `pay_channel` VARCHAR(20) DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `market_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `template_id` BIGINT NOT NULL,
    `template_title` VARCHAR(200) DEFAULT NULL,
    `template_price` DECIMAL(10,2) DEFAULT 0.00,
    `quantity` INT DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

CREATE TABLE IF NOT EXISTS `market_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_template` (`user_id`, `template_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板收藏表';

-- =====================================================
-- 5. Agent模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `asset_ai_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_id` BIGINT NOT NULL COMMENT '关联资产ID',
    `tool_type` VARCHAR(50) DEFAULT NULL COMMENT '工具类型: RUNWAY/JIMENG/MIDJOURNEY/SORA',
    `tool_version` VARCHAR(50) DEFAULT NULL COMMENT '工具版本',
    `prompt_text` TEXT DEFAULT NULL COMMENT '正向提示词',
    `negative_prompt` TEXT DEFAULT NULL COMMENT '反向提示词',
    `parameters` TEXT DEFAULT NULL COMMENT '生成参数JSON',
    `original_url` VARCHAR(500) DEFAULT NULL COMMENT '原始URL',
    `fps` INT DEFAULT 30 COMMENT '帧率',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI视频元数据表';

CREATE TABLE IF NOT EXISTS `video_frame` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `video_id` BIGINT NOT NULL COMMENT 'AI视频ID',
    `timestamp` DECIMAL(10,2) DEFAULT 0.00 COMMENT '时间戳(秒)',
    `frame_number` INT DEFAULT 0 COMMENT '帧序号',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `prompt_text` TEXT DEFAULT NULL COMMENT '帧提示词',
    `parameters` TEXT DEFAULT NULL COMMENT '帧参数',
    `is_keyframe` TINYINT DEFAULT 0 COMMENT '是否关键帧',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_is_keyframe` (`is_keyframe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频帧表';

CREATE TABLE IF NOT EXISTS `prompt_library` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `prompt_text` TEXT NOT NULL COMMENT '提示词文本',
    `prompt_type` VARCHAR(50) DEFAULT NULL COMMENT '类型',
    `style_tags` VARCHAR(500) DEFAULT NULL COMMENT '风格标签JSON数组',
    `source_video_id` BIGINT DEFAULT NULL COMMENT '来源视频ID',
    `source_frame_id` BIGINT DEFAULT NULL COMMENT '来源帧ID',
    `created_by` BIGINT DEFAULT NULL,
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_prompt_type` (`prompt_type`),
    KEY `idx_created_by` (`created_by`),
    FULLTEXT KEY `ft_prompt_text` (`prompt_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt库表';

CREATE TABLE IF NOT EXISTS `agent_data_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `platform` VARCHAR(50) DEFAULT NULL COMMENT '平台',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    `config_json` TEXT DEFAULT NULL COMMENT '配置JSON',
    `cron_expression` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式',
    `owner_id` BIGINT DEFAULT NULL,
    `last_execute_time` DATETIME DEFAULT NULL,
    `next_execute_time` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent数据采集任务表';

CREATE TABLE IF NOT EXISTS `agent_data_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `platform` VARCHAR(50) DEFAULT NULL,
    `work_id` VARCHAR(100) DEFAULT NULL COMMENT '作品ID',
    `raw_data` TEXT DEFAULT NULL COMMENT '原始数据JSON',
    `cleaned_data` TEXT DEFAULT NULL COMMENT '清洗后数据',
    `metrics` TEXT DEFAULT NULL COMMENT '指标JSON',
    `is_anomaly` TINYINT DEFAULT 0 COMMENT '是否异常',
    `anomaly_reason` VARCHAR(500) DEFAULT NULL COMMENT '异常原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_is_anomaly` (`is_anomaly`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent数据记录表';

CREATE TABLE IF NOT EXISTS `agent_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '报告标题',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '报告类型',
    `content` TEXT DEFAULT NULL COMMENT '报告内容JSON',
    `template_id` BIGINT DEFAULT NULL,
    `project_id` BIGINT DEFAULT NULL,
    `generated_by` BIGINT DEFAULT NULL,
    `generated_at` DATETIME DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent分析报告表';

CREATE TABLE IF NOT EXISTS `agent_report_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(200) NOT NULL,
    `type` VARCHAR(50) DEFAULT NULL,
    `template_content` TEXT DEFAULT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板表';

-- =====================================================
-- 6. 通知模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT DEFAULT NULL COMMENT '通知内容',
    `type` VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '类型',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- =====================================================
-- 7. 评审模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `review_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `asset_id` BIGINT NOT NULL COMMENT '资产ID',
    `user_id` BIGINT NOT NULL,
    `content` TEXT DEFAULT NULL COMMENT '批注内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审批注表';

CREATE TABLE IF NOT EXISTS `review_reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `comment_id` BIGINT NOT NULL COMMENT '批注ID',
    `user_id` BIGINT NOT NULL,
    `content` TEXT DEFAULT NULL COMMENT '回复内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审回复表';
