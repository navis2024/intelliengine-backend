-- =====================================================
-- 智擎 IntelliEngine 测试数据
--
-- 使用方式:
--   mysql -h 127.0.0.1 -P 3307 -u root -pIntelliEngine@2025 intelliengine < sql/test_data.sql
--
-- 注意: 所有测试用户密码均为 test123456
-- =====================================================

-- 先清理已存在的 test_admin 占用 id=1 的问题
DELETE FROM user_auth WHERE user_id = (SELECT id FROM (SELECT id FROM user_account WHERE username = 'test_admin') AS t);
DELETE FROM user_account WHERE username = 'test_admin';

-- =====================================================
-- 1. 测试用户 (5个)
-- =====================================================
INSERT INTO user_account (username, password_hash, email, nickname, user_type, status, created_at, updated_at) VALUES
('dev_zhang', '$2a$10$SU4fNLRv/6SD7lQTDSyTqO8ck4ccFE6M/CpAFNNJAotzIBBfHHZ2q', 'zhang@test.com', '张开发', 'PERSONAL', 1, NOW(), NOW()),
('dev_li', '$2a$10$SU4fNLRv/6SD7lQTDSyTqO8ck4ccFE6M/CpAFNNJAotzIBBfHHZ2q', 'li@test.com', '李测试', 'PERSONAL', 1, NOW(), NOW()),
('designer_wang', '$2a$10$SU4fNLRv/6SD7lQTDSyTqO8ck4ccFE6M/CpAFNNJAotzIBBfHHZ2q', 'wang@test.com', '王设计', 'ENTERPRISE', 1, NOW(), NOW()),
('pm_zhao', '$2a$10$SU4fNLRv/6SD7lQTDSyTqO8ck4ccFE6M/CpAFNNJAotzIBBfHHZ2q', 'zhao@test.com', '赵产品', 'PERSONAL', 1, NOW(), NOW()),
('ops_sun', '$2a$10$SU4fNLRv/6SD7lQTDSyTqO8ck4ccFE6M/CpAFNNJAotzIBBfHHZ2q', 'sun@test.com', '孙运维', 'ENTERPRISE', 1, NOW(), NOW());

-- 为每个用户创建密码认证记录
INSERT INTO user_auth (user_id, auth_type, auth_key, auth_secret, created_at)
SELECT id, 'PASSWORD', username, password_hash, NOW() FROM user_account WHERE is_deleted = 0;

-- =====================================================
-- 2. 项目数据 (5个)
--    使用子查询获取用户ID，避免硬编码
-- =====================================================
INSERT INTO project_info (project_code, name, description, owner_id, status, visibility, created_at, updated_at) VALUES
('PRJ-2025-001', '智能视频剪辑平台', '基于AI技术的智能视频剪辑与后期处理平台，支持自动字幕、场景识别等功能',
 (SELECT id FROM user_account WHERE username = 'dev_zhang'), 'ACTIVE', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('PRJ-2025-002', '电商产品展示网站', '为某品牌电商打造的3D产品展示网站，包含虚拟试穿功能',
 (SELECT id FROM user_account WHERE username = 'dev_li'), 'ACTIVE', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('PRJ-2025-003', '企业宣传片素材库', '公司2025年度宣传片所需的所有素材库，包括视频、图片、音频等',
 (SELECT id FROM user_account WHERE username = 'dev_zhang'), 'ACTIVE', 'PUBLIC', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('PRJ-2025-004', '老项目归档-冬季促销', '已完成归档的2024年冬季促销活动项目',
 (SELECT id FROM user_account WHERE username = 'designer_wang'), 'ARCHIVED', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 60 DAY)),
('PRJ-2025-005', '社交媒体短视频系列', '为某品牌制作的社交媒体短视频系列，计划发布在抖音、小红书等平台',
 (SELECT id FROM user_account WHERE username = 'pm_zhao'), 'ACTIVE', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- =====================================================
-- 3. 项目成员 (每个项目2-3人)
-- =====================================================
INSERT INTO project_member (project_id, user_id, role, joined_at) VALUES
-- 项目1: 张开发(OWNER), 李测试(MEMBER), 王设计(MEMBER)
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), (SELECT id FROM user_account WHERE username = 'dev_zhang'), 'OWNER', DATE_SUB(NOW(), INTERVAL 30 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), (SELECT id FROM user_account WHERE username = 'dev_li'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 28 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), (SELECT id FROM user_account WHERE username = 'designer_wang'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 25 DAY)),
-- 项目2: 李测试(OWNER), 赵产品(MEMBER)
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-002'), (SELECT id FROM user_account WHERE username = 'dev_li'), 'OWNER', DATE_SUB(NOW(), INTERVAL 20 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-002'), (SELECT id FROM user_account WHERE username = 'pm_zhao'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 18 DAY)),
-- 项目3: 张开发(OWNER), 孙运维(MEMBER), 王设计(MEMBER)
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), (SELECT id FROM user_account WHERE username = 'dev_zhang'), 'OWNER', DATE_SUB(NOW(), INTERVAL 15 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), (SELECT id FROM user_account WHERE username = 'ops_sun'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 14 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), (SELECT id FROM user_account WHERE username = 'designer_wang'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 12 DAY)),
-- 项目4: 王设计(OWNER), 张开发(MEMBER)
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-004'), (SELECT id FROM user_account WHERE username = 'designer_wang'), 'OWNER', DATE_SUB(NOW(), INTERVAL 90 DAY)),
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-004'), (SELECT id FROM user_account WHERE username = 'dev_zhang'), 'MEMBER', DATE_SUB(NOW(), INTERVAL 85 DAY)),
-- 项目5: 赵产品(OWNER)
((SELECT id FROM project_info WHERE project_code = 'PRJ-2025-005'), (SELECT id FROM user_account WHERE username = 'pm_zhao'), 'OWNER', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- =====================================================
-- 4. 测试资产
-- =====================================================
INSERT INTO asset_info (asset_code, name, type, owner_type, owner_id, version, status, file_url, file_size, file_format, duration, created_by, created_at, updated_at) VALUES
-- dev_zhang 的个人资产 (USER类型 — 用于资产管理页面测试)
('ASSET-USER-VID-001', '个人作品集-2025', 'VIDEO', 'USER', (SELECT id FROM user_account WHERE username = 'dev_zhang'), 2, 'PUBLISHED', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 52428800, 'mp4', 60, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('ASSET-USER-IMG-001', '个人Logo设计稿', 'IMAGE', 'USER', (SELECT id FROM user_account WHERE username = 'dev_zhang'), 1, 'PUBLISHED', 'https://picsum.photos/seed/mylogo/800/600', 1048576, 'png', NULL, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 项目1的资产 (owner_id = 项目ID，created_by = 用户ID)
('ASSET-VID-001', '产品宣传片-主视频', 'VIDEO', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), 3, 'PUBLISHED', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4', 52428800, 'mp4', 60, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('ASSET-IMG-001', '品牌Logo-高清版', 'IMAGE', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), 2, 'PUBLISHED', 'https://picsum.photos/seed/logo/800/600', 2048576, 'png', NULL, (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('ASSET-IMG-002', '产品渲染图-正面', 'IMAGE', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), 1, 'APPROVED', 'https://picsum.photos/seed/product1/1920/1080', 3145728, 'jpg', NULL, (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ASSET-AUD-001', '背景音乐-轻快版', 'AUDIO', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-001'), 1, 'PUBLISHED', 'https://www.w3schools.com/html/horse.mp3', 5242880, 'mp3', 30, (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 项目2的资产
('ASSET-IMG-003', '产品主图-白色背景', 'IMAGE', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-002'), 2, 'PUBLISHED', 'https://picsum.photos/seed/product2/1200/1200', 4194304, 'jpg', NULL, (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('ASSET-IMG-004', '产品细节图-特写', 'IMAGE', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-002'), 1, 'APPROVED', 'https://picsum.photos/seed/detail/1600/900', 3145728, 'jpg', NULL, (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 项目3的资产
('ASSET-VID-002', '宣传片-航拍素材', 'VIDEO', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), 1, 'PENDING_REVIEW', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4', 104857600, 'mp4', 120, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('ASSET-IMG-005', '企业活动照片合集', 'IMAGE', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), 1, 'DRAFT', 'https://picsum.photos/seed/event/2000/1333', 5242880, 'jpg', NULL, (SELECT id FROM user_account WHERE username = 'ops_sun'), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('ASSET-AUD-002', '采访录音-CEO访谈', 'AUDIO', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-003'), 1, 'DRAFT', 'https://www.w3schools.com/html/horse.mp3', 15728640, 'mp3', 180, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- 项目5的资产
('ASSET-VID-003', '抖音短视频-第一集', 'VIDEO', 'PROJECT', (SELECT id FROM project_info WHERE project_code = 'PRJ-2025-005'), 1, 'DRAFT', 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4', 31457280, 'mp4', 45, (SELECT id FROM user_account WHERE username = 'pm_zhao'), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- 资产版本记录
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '初始版本', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 28 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-USER-VID-001';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 2, '更新字幕和效果', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-USER-VID-001';

INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '初始版本 - 粗剪', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 28 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-VID-001';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 2, '添加字幕和转场效果', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 15 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-VID-001';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 3, '最终版本 - 加入背景音乐', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-VID-001';

INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '原始Logo上传', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 25 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-001';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 2, '重新设计 - 扁平化风格', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-001';

INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '初始渲染图', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 22 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-002';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '背景音乐初版', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 20 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-AUD-001';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '产品主图初版', (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 18 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-003';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 2, '增加背景虚化效果', (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-003';
INSERT INTO asset_version (asset_id, version_number, change_log, created_by, created_at)
SELECT a.id, 1, '产品细节特写', (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 16 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-IMG-004';

-- =====================================================
-- 5. 模板市场数据 (8个模板)
-- =====================================================
INSERT INTO asset_info (asset_code, name, type, owner_type, owner_id, version, status, file_url, file_size, file_format, duration, created_by, created_at, updated_at) VALUES
('TMPL-ASSET-001', '电商产品展示模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl1/800/600', 1048576, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('TMPL-ASSET-002', '企业宣传片模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl2/800/600', 2097152, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('TMPL-ASSET-003', '社交媒体短视频模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl3/800/600', 524288, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TMPL-ASSET-004', '产品开箱视频模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl4/800/600', 1572864, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO market_template (asset_id, title, description, price, original_price, sales_count, view_count, rating, status, created_by, created_at, updated_at) VALUES
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-001'), '电商产品展示模板', '适用于各类电商产品的3D展示模板，支持360度旋转查看。包含5种预设动画效果', 99.00, 199.00, 128, 4520, 4.8, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-002'), '企业宣传片模板', '专业级宣传片模板，包含片头动画、产品展示、团队介绍等模块', 299.00, 599.00, 56, 2340, 4.6, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'dev_li'), DATE_SUB(NOW(), INTERVAL 55 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-003'), '社交媒体短视频模板', '适配抖音、快手、小红书等平台的短视频模板，含热门转场特效', 49.00, 99.00, 342, 12580, 4.9, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 50 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-004'), '产品开箱视频模板', '适用于数码产品、美妆等品类的开箱视频模板', 79.00, 159.00, 89, 3890, 4.7, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'designer_wang'), DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY));

INSERT INTO asset_info (asset_code, name, type, owner_type, owner_id, version, status, file_url, file_size, file_format, duration, created_by, created_at, updated_at) VALUES
('TMPL-ASSET-005', '简约片头模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl5/800/600', 262144, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'pm_zhao'), DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TMPL-ASSET-006', '春季节日促销模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl6/800/600', 1048576, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'ops_sun'), DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('TMPL-ASSET-007', 'Vlog日常记录模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl7/800/600', 786432, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
('TMPL-ASSET-008', '商务演示PPT模板', 'TEMPLATE', 'PLATFORM', 1, 1, 'PUBLISHED', 'https://picsum.photos/seed/tmpl8/800/600', 3145728, 'zip', NULL, (SELECT id FROM user_account WHERE username = 'pm_zhao'), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));

INSERT INTO market_template (asset_id, title, description, price, original_price, sales_count, view_count, rating, status, created_by, created_at, updated_at) VALUES
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-005'), '简约片头模板', '极简风格的视频片头模板，包含3种动画风格', 0.00, 29.00, 567, 18900, 4.5, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'pm_zhao'), DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-006'), '春季节日促销模板', '专为春节/节日促销设计的视频模板', 19.00, 49.00, 234, 8900, 4.3, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'ops_sun'), DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-007'), 'Vlog日常记录模板', '适合日常Vlog记录的轻量模板，日系小清新风格', 0.00, 39.00, 890, 25600, 4.7, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'dev_zhang'), DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
((SELECT id FROM asset_info WHERE asset_code = 'TMPL-ASSET-008'), '商务演示PPT模板', '专业商务风格PPT模板，适用于项目汇报、方案演示等场景', 39.00, 79.00, 178, 6700, 4.4, 'PUBLISHED', (SELECT id FROM user_account WHERE username = 'pm_zhao'), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));

-- =====================================================
-- 6. Agent 测试数据
-- =====================================================

-- 6.1 AI 视频元数据
INSERT INTO asset_ai_video (asset_id, tool_type, tool_version, prompt_text, negative_prompt, parameters, original_url, fps, created_at)
SELECT a.id, 'RUNWAY', 'Gen-3', 'Cinematic aerial shot of a modern city skyline at sunset, warm golden light, 4k, photorealistic', 'blurry, low quality, distortion', '{"seed": 12345, "steps": 50, "cfg": 7.5}', 'https://runwayml.com/generate/video-abc123', 30, DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-VID-001';

INSERT INTO asset_ai_video (asset_id, tool_type, tool_version, prompt_text, negative_prompt, parameters, original_url, fps, created_at)
SELECT a.id, 'JIMENG', 'v2', 'Product showcase rotating on a turntable, studio lighting, white background, 60fps', 'shadow, reflection, text', '{"seed": 67890, "steps": 40, "cfg": 6.0}', 'https://jimeng.ai/video/def456', 60, DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM asset_info a WHERE a.asset_code = 'ASSET-VID-003';

-- 6.2 视频帧数据
INSERT INTO video_frame (video_id, timestamp, frame_number, thumbnail_url, prompt_text, parameters, is_keyframe, tags, created_at)
SELECT av.id, 0.00, 0, 'https://picsum.photos/seed/frame0/320/180', 'City skyline panorama establishing shot', NULL, 1, '开场,全景', DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM asset_ai_video av JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-001';

INSERT INTO video_frame (video_id, timestamp, frame_number, thumbnail_url, prompt_text, parameters, is_keyframe, tags, created_at)
SELECT av.id, 15.00, 45, 'https://picsum.photos/seed/frame45/320/180', 'Close-up of glass building reflection, sun flare', NULL, 1, '转场,细节', DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM asset_ai_video av JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-001';

INSERT INTO video_frame (video_id, timestamp, frame_number, thumbnail_url, prompt_text, parameters, is_keyframe, tags, created_at)
SELECT av.id, 30.00, 90, 'https://picsum.photos/seed/frame90/320/180', 'Drone shot pulling up to reveal full skyline', NULL, 1, '高潮,全景', DATE_SUB(NOW(), INTERVAL 7 DAY)
FROM asset_ai_video av JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-001';

INSERT INTO video_frame (video_id, timestamp, frame_number, thumbnail_url, prompt_text, parameters, is_keyframe, tags, created_at)
SELECT av.id, 0.00, 0, 'https://picsum.photos/seed/v3frame0/320/180', 'Product on white turntable, studio lighting', NULL, 1, '开场,产品', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM asset_ai_video av JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-003';

-- 6.3 Prompt 库数据
INSERT INTO prompt_library (prompt_text, prompt_type, style_tags, source_video_id, source_frame_id, created_by, use_count, rating, created_at, updated_at) VALUES
('Cinematic aerial shot of city skyline at sunset, warm golden light, 4k, photorealistic', 'TEXT_TO_VIDEO', '["cinematic","aerial","sunset","photorealistic"]',
 (SELECT av.id FROM asset_ai_video av JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-001'),
 (SELECT f.id FROM video_frame f JOIN asset_ai_video av ON f.video_id = av.id JOIN asset_info a ON av.asset_id = a.id WHERE a.asset_code = 'ASSET-VID-001' AND f.frame_number = 0),
 (SELECT id FROM user_account WHERE username = 'dev_zhang'), 5, 4.5, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),

('Anime style girl walking through neon-lit street at night, detailed background, cyberpunk aesthetic', 'TEXT_TO_VIDEO', '["anime","cyberpunk","neon","night"]',
 NULL, NULL,
 (SELECT id FROM user_account WHERE username = 'dev_zhang'), 12, 4.8, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

('Professional product photography, white background, studio lighting, high detail, 8k', 'TEXT_TO_IMAGE', '["product","studio","professional","8k"]',
 NULL, NULL,
 (SELECT id FROM user_account WHERE username = 'designer_wang'), 8, 4.2, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW()),

('Watercolor painting of a misty mountain landscape, soft colors, ethereal atmosphere', 'TEXT_TO_IMAGE', '["watercolor","landscape","misty","artistic"]',
 NULL, NULL,
 (SELECT id FROM user_account WHERE username = 'designer_wang'), 3, 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

('Smooth camera pan across a futuristic interior, holographic displays, blue and purple lighting', 'TEXT_TO_VIDEO', '["futuristic","sci-fi","interior","holographic"]',
 NULL, NULL,
 (SELECT id FROM user_account WHERE username = 'pm_zhao'), 7, 4.6, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- =====================================================
-- 7. 更新统计信息
-- =====================================================
UPDATE user_account SET last_login_at = DATE_SUB(NOW(), INTERVAL 1 HOUR) WHERE username = 'dev_zhang';
UPDATE user_account SET last_login_at = DATE_SUB(NOW(), INTERVAL 2 HOUR) WHERE username = 'dev_li';
UPDATE user_account SET last_login_at = DATE_SUB(NOW(), INTERVAL 3 HOUR) WHERE username = 'designer_wang';
UPDATE user_account SET last_login_at = DATE_SUB(NOW(), INTERVAL 1 DAY) WHERE username = 'pm_zhao';
UPDATE user_account SET last_login_at = DATE_SUB(NOW(), INTERVAL 2 DAY) WHERE username = 'ops_sun';

SELECT '=== 测试数据插入完成 ===' AS result;
SELECT CONCAT('用户数: ', COUNT(*)) FROM user_account WHERE is_deleted = 0;
SELECT CONCAT('项目数: ', COUNT(*)) FROM project_info WHERE is_deleted = 0;
SELECT CONCAT('成员数: ', COUNT(*)) FROM project_member;
SELECT CONCAT('资产数: ', COUNT(*)) FROM asset_info WHERE is_deleted = 0;
SELECT CONCAT('模板数: ', COUNT(*)) FROM market_template;
SELECT CONCAT('AI视频数: ', COUNT(*)) FROM asset_ai_video;
SELECT CONCAT('视频帧数: ', COUNT(*)) FROM video_frame;
SELECT CONCAT('Prompt数: ', COUNT(*)) FROM prompt_library;
