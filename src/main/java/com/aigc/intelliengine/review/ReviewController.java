package com.aigc.intelliengine.review;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.review.model.dto.CommentCreateRequest;
import com.aigc.intelliengine.review.model.dto.ReplyCreateRequest;
import com.aigc.intelliengine.review.model.vo.ReviewCommentVO;
import com.aigc.intelliengine.review.model.vo.ReviewReplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "审阅批注管理")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/comments")
    @Operation(summary = "创建评论/批注")
    public ApiResponse<ReviewCommentVO> createComment(@Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(reviewService.createComment(request, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/assets/{assetId}/comments")
    @Operation(summary = "获取资产的所有评论")
    public ApiResponse<List<ReviewCommentVO>> getComments(@PathVariable Long assetId) {
        return ApiResponse.success(reviewService.getCommentsByAsset(assetId, UserContextHolder.getCurrentUserId()));
    }

    @PutMapping("/comments/{id}/status")
    @Operation(summary = "更新评论状态")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reviewService.updateCommentStatus(id, body.get("status"), UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        reviewService.deleteComment(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @PostMapping("/comments/{id}/reply")
    @Operation(summary = "回复评论")
    public ApiResponse<ReviewReplyVO> createReply(@PathVariable Long id, @Valid @RequestBody ReplyCreateRequest request) {
        return ApiResponse.success(reviewService.createReply(id, request, UserContextHolder.getCurrentUserId()));
    }

    @PutMapping("/replies/{id}")
    @Operation(summary = "修改回复")
    public ApiResponse<ReviewReplyVO> updateReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.success(reviewService.updateReply(id, body.get("content"), UserContextHolder.getCurrentUserId()));
    }

    @DeleteMapping("/replies/{id}")
    @Operation(summary = "删除回复")
    public ApiResponse<Void> deleteReply(@PathVariable Long id) {
        reviewService.deleteReply(id, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }
}
