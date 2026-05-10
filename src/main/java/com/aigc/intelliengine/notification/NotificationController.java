package com.aigc.intelliengine.notification;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.notification.model.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "通知管理")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "我的通知列表")
    public ApiResponse<PageResult<NotificationVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(notificationService.listByUser(UserContextHolder.getCurrentUserId(), pageNum, pageSize));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead(UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }
}
