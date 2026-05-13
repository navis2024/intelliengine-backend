UPDATE video_frame SET prompt_text = '中景，东方奇幻水境，白衣人静坐与巨狐对视，暗红背景白烟缭绕，柔光侧照，红白对比色调诡丽神秘。' WHERE id = (SELECT id FROM (SELECT id FROM video_frame WHERE video_id=5 AND frame_number=48 LIMIT 1) t);
UPDATE video_frame SET prompt_text = '特写，东方志怪风，白狐妖面，额饰红宝，金瞳，手托下颌，烟雾莲花环绕，粉紫色调，柔光，神秘妖异。' WHERE id = (SELECT id FROM (SELECT id FROM video_frame WHERE video_id=5 AND frame_number=72 LIMIT 1) t);
