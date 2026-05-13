ALTER TABLE asset_version ADD COLUMN agent_advice TEXT DEFAULT NULL COMMENT 'AI Agent生成建议(JSON)' AFTER change_log;
ALTER TABLE review_comment ADD COLUMN frame_number INT DEFAULT NULL COMMENT '关联视频帧号' AFTER position_y;
