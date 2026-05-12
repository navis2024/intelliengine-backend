-- =====================================================
-- 智擎 IntelliEngine - 完整数据库初始化脚本
-- 版本: v2.0.0
-- 日期: 2026-05-11
-- 数据库: intelliengine
-- 说明: 与当前数据库实际结构同步
-- =====================================================

-- =====================================================
-- 1. 用户模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_account` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'BCrypt密码哈希',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `user_type` VARCHAR(20) DEFAULT 'PERSONAL' COMMENT '用户类型: PERSONAL/ENTERPRISE',
    `status` TINYINT UNSIGNED DEFAULT 1 COMMENT '状态: 0-禁用 1-正常 2-未激活',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

CREATE TABLE IF NOT EXISTS `user_auth` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '认证记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `auth_type` VARCHAR(20) NOT NULL COMMENT '认证类型: PASSWORD/WECHAT/GITHUB',
    `auth_key` VARCHAR(100) NOT NULL COMMENT '认证标识',
    `auth_secret` VARCHAR(255) DEFAULT NULL COMMENT '认证密钥',
    `extra_data` JSON DEFAULT NULL COMMENT '额外数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth` (`auth_type`, `auth_key`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证表';

-- =====================================================
-- 2. 项目管理模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `project_info` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `project_code` VARCHAR(50) NOT NULL COMMENT '项目编码',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` TEXT DEFAULT NULL COMMENT '项目描述',
    `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '负责人ID',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/ARCHIVED/DELETED',
    `visibility` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '可见性: PRIVATE/PUBLIC',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_code` (`project_code`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目信息表';

CREATE TABLE IF NOT EXISTS `project_member` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '成员记录ID',
    `project_id` BIGINT UNSIGNED NOT NULL COMMENT '项目ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色: OWNER/ADMIN/EDITOR/VIEWER/MEMBER',
    `joined_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- =====================================================
-- 3. 资产管理模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `asset_info` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '资产ID',
    `asset_code` VARCHAR(50) NOT NULL COMMENT '资产编码',
    `name` VARCHAR(200) NOT NULL COMMENT '资产名称',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: VIDEO/IMAGE/AUDIO/TEMPLATE',
    `owner_type` VARCHAR(20) NOT NULL COMMENT '所属类型: USER/PROJECT/PLATFORM',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '所有者ID',
    `source_asset_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '源资产ID',
    `source_version` INT DEFAULT NULL COMMENT '源版本号',
    `version` INT DEFAULT 1 COMMENT '当前版本号',
    `is_latest` TINYINT UNSIGNED DEFAULT 1 COMMENT '是否最新版本',
    `commit_message` VARCHAR(500) DEFAULT NULL COMMENT '提交信息',
    `committed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '提交者',
    `committed_at` DATETIME DEFAULT NULL COMMENT '提交时间',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/UPLOADING/READY/ARCHIVED',
    `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    `file_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小(字节)',
    `file_format` VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    `duration` INT DEFAULT NULL COMMENT '时长(秒)',
    `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_code` (`asset_code`),
    KEY `idx_owner` (`owner_type`, `owner_id`),
    KEY `idx_source` (`source_asset_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产信息表';

CREATE TABLE IF NOT EXISTS `asset_version` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本记录ID',
    `asset_id` BIGINT NOT NULL COMMENT '资产ID',
    `version_number` INT NOT NULL COMMENT '版本号',
    `snapshot_data` JSON DEFAULT NULL COMMENT '快照数据',
    `change_log` TEXT DEFAULT NULL COMMENT '变更日志',
    `created_by` BIGINT NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产版本表';

-- =====================================================
-- 4. 模板市场模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `market_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `asset_id` BIGINT UNSIGNED NOT NULL COMMENT '关联资产ID',
    `title` VARCHAR(200) NOT NULL COMMENT '模板标题',
    `description` TEXT DEFAULT NULL COMMENT '模板描述',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    `sales_count` INT UNSIGNED DEFAULT 0 COMMENT '销售数量',
    `view_count` INT UNSIGNED DEFAULT 0 COMMENT '浏览次数',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PUBLISHED/OFF_SHELF',
    `created_by` BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_id` (`asset_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板市场表';

CREATE TABLE IF NOT EXISTS `market_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '货币',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/COMPLETED/CANCELLED',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `pay_channel` VARCHAR(20) DEFAULT NULL COMMENT '支付渠道',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `market_order_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT '模板ID',
    `template_title` VARCHAR(200) DEFAULT NULL COMMENT '模板标题快照',
    `template_price` DECIMAL(10,2) DEFAULT NULL COMMENT '模板价格快照',
    `quantity` INT UNSIGNED DEFAULT 1 COMMENT '数量',
    `subtotal` DECIMAL(10,2) DEFAULT NULL COMMENT '小计',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

CREATE TABLE IF NOT EXISTS `market_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_template` (`user_id`, `template_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板收藏表';

-- =====================================================
-- 5. Agent模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `asset_ai_video` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'AI视频元数据ID',
    `asset_id` BIGINT NOT NULL COMMENT '关联资产ID',
    `tool_type` VARCHAR(50) DEFAULT NULL COMMENT '生成工具: RUNWAY/PIKA/MIDJOURNEY/JIMENG',
    `tool_version` VARCHAR(50) DEFAULT NULL COMMENT '工具版本',
    `prompt_text` TEXT DEFAULT NULL COMMENT '主提示词',
    `negative_prompt` TEXT DEFAULT NULL COMMENT '反向提示词',
    `parameters` JSON DEFAULT NULL COMMENT '生成参数',
    `original_url` VARCHAR(500) DEFAULT NULL COMMENT '原始URL',
    `fps` INT DEFAULT NULL COMMENT '帧率',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_asset_id` (`asset_id`),
    KEY `idx_tool_type` (`tool_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI视频元数据表';

CREATE TABLE IF NOT EXISTS `video_frame` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帧记录ID',
    `video_id` BIGINT NOT NULL COMMENT 'AI视频ID',
    `timestamp` DECIMAL(8,2) NOT NULL COMMENT '时间点(秒)',
    `frame_number` INT DEFAULT NULL COMMENT '帧序号',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `prompt_text` TEXT DEFAULT NULL COMMENT '帧提示词',
    `parameters` JSON DEFAULT NULL COMMENT '帧参数',
    `is_keyframe` TINYINT DEFAULT 0 COMMENT '是否关键帧',
    `tags` VARCHAR(200) DEFAULT NULL COMMENT '标签',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_keyframe` (`is_keyframe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频帧表';

CREATE TABLE IF NOT EXISTS `prompt_library` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Prompt记录ID',
    `prompt_text` TEXT NOT NULL COMMENT '提示词文本',
    `prompt_type` VARCHAR(50) DEFAULT NULL COMMENT '类型: IMAGE/VIDEO/AUDIO',
    `style_tags` JSON DEFAULT NULL COMMENT '风格标签',
    `source_video_id` BIGINT DEFAULT NULL COMMENT '来源视频ID',
    `source_frame_id` BIGINT DEFAULT NULL COMMENT '来源帧ID',
    `created_by` BIGINT NOT NULL COMMENT '创建者ID',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `rating` DECIMAL(2,1) DEFAULT 5.0 COMMENT '评分',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`prompt_type`),
    FULLTEXT KEY `idx_prompt` (`prompt_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt库表';

CREATE TABLE IF NOT EXISTS `agent_data_task` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/COMPLETED/FAILED',
    `config_json` JSON DEFAULT NULL COMMENT '配置JSON',
    `schedule_type` VARCHAR(20) DEFAULT NULL COMMENT '调度类型',
    `cron_expression` VARCHAR(50) DEFAULT NULL COMMENT 'Cron表达式',
    `last_execute_time` DATETIME DEFAULT NULL COMMENT '上次执行时间',
    `next_execute_time` DATETIME DEFAULT NULL COMMENT '下次执行时间',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '创建者ID',
    `owner_type` VARCHAR(20) DEFAULT NULL COMMENT '所有者类型',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_platform` (`platform`),
    KEY `idx_status` (`status`),
    KEY `idx_owner` (`owner_id`, `owner_type`),
    KEY `idx_next_execute` (`next_execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent数据采集任务表';

CREATE TABLE IF NOT EXISTS `agent_data_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `task_id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台',
    `work_id` VARCHAR(64) DEFAULT NULL COMMENT '作品ID',
    `raw_data` JSON DEFAULT NULL COMMENT '原始数据',
    `cleaned_data` JSON DEFAULT NULL COMMENT '清洗后数据',
    `metrics` JSON DEFAULT NULL COMMENT '解析指标',
    `status` TINYINT UNSIGNED DEFAULT 0 COMMENT '状态',
    `is_anomaly` TINYINT UNSIGNED DEFAULT 0 COMMENT '是否异常',
    `anomaly_reason` VARCHAR(200) DEFAULT NULL COMMENT '异常原因',
    `collected_at` DATETIME NOT NULL COMMENT '采集时间',
    `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_platform_work` (`platform`, `work_id`),
    KEY `idx_collected_at` (`collected_at`),
    KEY `idx_status_anomaly` (`status`, `is_anomaly`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent数据记录表';

CREATE TABLE IF NOT EXISTS `agent_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '报告标题',
    `type` VARCHAR(50) DEFAULT NULL COMMENT '报告类型',
    `content` TEXT DEFAULT NULL COMMENT '报告内容JSON',
    `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
    `project_id` BIGINT DEFAULT NULL COMMENT '项目ID',
    `generated_by` BIGINT DEFAULT NULL COMMENT '生成者ID',
    `generated_at` DATETIME DEFAULT NULL COMMENT '生成时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agent分析报告表';

CREATE TABLE IF NOT EXISTS `agent_report_template` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(20) NOT NULL COMMENT '类型',
    `content_template` TEXT DEFAULT NULL COMMENT '内容模板',
    `chart_configs` JSON DEFAULT NULL COMMENT '图表配置',
    `default_format` VARCHAR(20) DEFAULT 'MARKDOWN' COMMENT '默认格式',
    `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`template_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报告模板表';

-- =====================================================
-- 6. 通知模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `recipient_id` BIGINT NOT NULL COMMENT '接收人ID',
    `notification_type` VARCHAR(30) NOT NULL COMMENT '类型: REVIEW_REPLY/TASK_COMPLETE/ORDER_STATUS/REPORT_READY/SYSTEM',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT DEFAULT NULL COMMENT '通知内容',
    `related_type` VARCHAR(30) DEFAULT NULL COMMENT '关联类型: PROJECT/ASSET/ORDER/TASK',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_recipient` (`recipient_id`, `is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- =====================================================
-- 7. 评审模块
-- =====================================================
CREATE TABLE IF NOT EXISTS `review_comment` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '批注ID',
    `asset_id` BIGINT UNSIGNED NOT NULL COMMENT '关联资产ID',
    `project_id` BIGINT UNSIGNED NOT NULL COMMENT '所属项目ID',
    `content` TEXT NOT NULL COMMENT '批注内容',
    `comment_type` VARCHAR(20) DEFAULT 'COMMENT' COMMENT '类型: COMMENT/SUGGESTION/ISSUE',
    `timestamp` DECIMAL(8,2) DEFAULT NULL COMMENT '视频时间点(秒)',
    `position_x` DECIMAL(5,2) DEFAULT NULL COMMENT '画布X坐标',
    `position_y` DECIMAL(5,2) DEFAULT NULL COMMENT '画布Y坐标',
    `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态: OPEN/RESOLVED/CLOSED',
    `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT UNSIGNED DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_asset_id` (`asset_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_timestamp` (`timestamp`),
    KEY `idx_status` (`status`),
    KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审批注表';

CREATE TABLE IF NOT EXISTS `review_reply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回复ID',
    `comment_id` BIGINT NOT NULL COMMENT '批注ID',
    `content` TEXT NOT NULL COMMENT '回复内容',
    `created_by` BIGINT NOT NULL COMMENT '回复人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_comment_id` (`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评审回复表';
