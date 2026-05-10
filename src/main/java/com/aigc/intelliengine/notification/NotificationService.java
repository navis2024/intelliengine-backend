package com.aigc.intelliengine.notification;

import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.notification.model.entity.Notification;
import com.aigc.intelliengine.notification.model.vo.NotificationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public PageResult<NotificationVO> listByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).orderByDesc(Notification::getCreatedAt);
        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toVO).toList(), page.getTotal(), pageNum, pageSize);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id).eq(Notification::getUserId, userId).set(Notification::getIsRead, 1));
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId).set(Notification::getIsRead, 1));
    }

    private NotificationVO toVO(Notification n) {
        if (n == null) return null;
        NotificationVO vo = new NotificationVO();
        vo.setId(String.valueOf(n.getId()));
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setType(n.getType());
        vo.setIsRead(n.getIsRead());
        vo.setRelatedId(n.getRelatedId());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
