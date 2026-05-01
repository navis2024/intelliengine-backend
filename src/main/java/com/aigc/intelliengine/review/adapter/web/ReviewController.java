package com.aigc.intelliengine.review.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.review.app.service.ReviewAppService;
import com.aigc.intelliengine.review.dto.CommentCreateRequest;
import com.aigc.intelliengine.review.vo.ReviewCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "审阅批注", description = "视频批注、评论管理")
public class ReviewController {
    private final ReviewAppService reviewAppService;
    
    @PostMapping("/comments")
    @Operation(summary = "创建批注")
    public ApiResponse<ReviewCommentVO> createComment(@Valid @RequestBody CommentCreateRequest request) {
        Long userId = 1L; // TODO: 从token获取
        return ApiResponse.success(reviewAppService.createComment(request, userId));
    }
    
    @GetMapping("/assets/{assetId}/comments")
    @Operation(summary = "获取资产的批注列表")
    public ApiResponse<List<ReviewCommentVO>> getAssetComments(@PathVariable Long assetId) {
        return ApiResponse.success(reviewAppService.getCommentsByAsset(assetId));
    }
}
