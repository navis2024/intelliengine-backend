DELETE FROM project_member WHERE project_id IN (9,10,11,12,13);
DELETE FROM project_info WHERE id IN (9,10,11,12,13);

INSERT INTO project_info (project_code, name, description, owner_id, status, visibility, created_at, updated_at)
VALUES ('PRJ-2026-NTM', '南天门计划', '中国空天母舰概念视频，科幻军事风格', 10, 'ACTIVE', 'PRIVATE', NOW(), NOW());

SET @new_id = LAST_INSERT_ID();
INSERT INTO project_member (project_id, user_id, role, joined_at) VALUES (@new_id, 10, 'OWNER', NOW());
UPDATE asset_info SET owner_type = 'PROJECT', owner_id = @new_id WHERE id = 30;

SELECT @new_id AS new_project_id;
