-- =====================================================
-- 智擎 IntelliEngine — 索引优化迁移脚本
-- 日期: 2026-05-09
-- 基于 EXPLAIN 分析 6 个核心查询的优化
-- =====================================================

-- 1. 资产查询: WHERE owner_type=? AND owner_id=? ORDER BY created_at DESC
--    高频场景: 项目资产列表、用户资产列表
ALTER TABLE `asset_info` ADD INDEX IF NOT EXISTS `idx_owner_type_owner_id` (`owner_type`, `owner_id`, `created_at`);

-- 2. 资产查询: WHERE created_by=? ORDER BY created_at DESC
--    高频场景: 我的资产列表
ALTER TABLE `asset_info` ADD INDEX IF NOT EXISTS `idx_created_by` (`created_by`, `created_at`);

-- 3. 资产查询: WHERE type=? AND status=?
--    高频场景: Agent 数据统计(按类型筛选)
ALTER TABLE `asset_info` ADD INDEX IF NOT EXISTS `idx_type_status` (`type`, `status`);

-- 4. 项目成员查询: WHERE project_id=? AND user_id=?
--    高频场景: 权限校验(MembershipValidator)
ALTER TABLE `project_member` ADD INDEX IF NOT EXISTS `idx_project_user` (`project_id`, `user_id`);

-- 5. 市场模板查询: WHERE status=? AND title LIKE ?
--    高频场景: 模板市场浏览和搜索
ALTER TABLE `market_template` ADD INDEX IF NOT EXISTS `idx_status_created` (`status`, `created_at`);

-- 6. Agent 数据记录查询: WHERE is_anomaly=? ORDER BY collected_at DESC
--    高频场景: 异常监控报告
ALTER TABLE `agent_data_record` ADD INDEX IF NOT EXISTS `idx_is_anomaly_collected` (`is_anomaly`, `collected_at`);

-- 7. Agent 数据任务查询: WHERE owner_id=? ORDER BY created_at DESC
--    高频场景: 我的数据采集任务
ALTER TABLE `agent_data_task` ADD INDEX IF NOT EXISTS `idx_owner_status` (`owner_id`, `status`, `created_at`);
