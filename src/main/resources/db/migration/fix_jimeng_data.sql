-- Fix corrupted Chinese data: delete bad records, re-insert with proper encoding
DELETE FROM video_frame WHERE video_id = 5;

UPDATE asset_info SET name = '即梦赛博朋克城市夜景' WHERE id = 28;

INSERT INTO video_frame (video_id, timestamp, frame_number, thumbnail_url, prompt_text, parameters, is_keyframe, tags, created_at) VALUES
(5, 0.00, 1, 'https://picsum.photos/seed/jimeng_f1/640/360', '广角镜头：赛博朋克城市全景，霓虹灯招牌闪烁，远处高楼耸立，雨刚停的街道反射着五颜六色的灯光', NULL, 1, '开场,全景,广角', NOW()),
(5, 3.50, 2, 'https://picsum.photos/seed/jimeng_f2/640/360', '中景跟拍：飞行汽车在楼宇间穿梭，尾灯拖出光带，空气中有细小的雨滴', NULL, 0, '跟拍,动态,飞行汽车', NOW()),
(5, 7.00, 3, 'https://picsum.photos/seed/jimeng_f3/640/360', '特写镜头：主角站在天桥上，霓虹灯从侧面勾勒出轮廓，眼中反射城市灯光', NULL, 1, '特写,人物,关键帧', NOW()),
(5, 10.50, 4, 'https://picsum.photos/seed/jimeng_f4/640/360', '俯拍：城市街道车流如织，光轨在夜色中交织，形成赛博朋克标志性的光影效果', NULL, 0, '俯拍,光轨,夜景', NOW()),
(5, 14.00, 5, 'https://picsum.photos/seed/jimeng_f5/640/360', '慢动作：雨滴落在水洼中，激起涟漪，倒映着上方的全息广告牌和霓虹光', NULL, 1, '慢动作,雨滴,全息,关键帧', NOW()),
(5, 17.50, 6, 'https://picsum.photos/seed/jimeng_f6/640/360', '广角镜头：城市天际线在黄昏与黑夜交界，最后一抹紫红色晚霞消失在高楼背后', NULL, 0, '广角,黄昏,天际线', NOW()),
(5, 21.00, 7, 'https://picsum.photos/seed/jimeng_f7/640/360', '近景：街边摊贩的全息菜单在空中浮动，蒸汽从食物摊位升起，融入霓虹灯光中', NULL, 1, '近景,全息,街景,关键帧', NOW());
