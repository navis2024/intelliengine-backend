package com.aigc.intelliengine.market;

import com.aigc.intelliengine.common.model.ApiResponse;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.security.UserContextHolder;
import com.aigc.intelliengine.market.model.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.model.dto.OrderCreateRequest;
import com.aigc.intelliengine.market.model.vo.MarketOrderVO;
import com.aigc.intelliengine.market.model.vo.MarketTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/market")
@RequiredArgsConstructor
@Tag(name = "Market", description = "模板市场")
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/templates")
    @Operation(summary = "模板列表")
    public ApiResponse<PageResult<MarketTemplateVO>> listTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "12") Integer pageSize) {
        return ApiResponse.success(marketService.listTemplates(keyword, sort, pageNum, pageSize));
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "模板详情")
    public ApiResponse<MarketTemplateVO> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(marketService.getTemplate(id));
    }

    @PostMapping("/templates")
    @Operation(summary = "创建模板")
    public ApiResponse<MarketTemplateVO> createTemplate(@Valid @RequestBody MarketTemplateCreateRequest request) {
        return ApiResponse.success(marketService.createTemplate(request, UserContextHolder.getCurrentUserId()));
    }

    @PostMapping("/orders")
    @Operation(summary = "创建订单")
    public ApiResponse<MarketOrderVO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(marketService.createOrder(request, UserContextHolder.getCurrentUserId()));
    }

    @GetMapping("/orders")
    @Operation(summary = "我的订单")
    public ApiResponse<PageResult<MarketOrderVO>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return ApiResponse.success(marketService.getMyOrders(UserContextHolder.getCurrentUserId(), pageNum, pageSize));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "订单详情")
    public ApiResponse<MarketOrderVO> getOrder(@PathVariable Long id) {
        return ApiResponse.success(marketService.getOrder(id, UserContextHolder.getCurrentUserId()));
    }

    @PutMapping("/templates/{id}")
    @Operation(summary = "更新模板")
    public ApiResponse<MarketTemplateVO> updateTemplate(@PathVariable Long id, @RequestBody MarketTemplateCreateRequest request) {
        return ApiResponse.success(marketService.updateTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    @Operation(summary = "删除模板")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        marketService.deleteTemplate(id);
        return ApiResponse.success();
    }

    @PutMapping("/orders/{id}/cancel")
    @Operation(summary = "取消订单")
    public ApiResponse<Void> cancelOrder(@PathVariable Long id) {
        marketService.cancelOrder(id);
        return ApiResponse.success();
    }

    @PostMapping("/favorites")
    @Operation(summary = "添加收藏")
    public ApiResponse<Void> addFavorite(@RequestParam Long templateId) {
        marketService.addFavorite(templateId, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/favorites/{templateId}")
    @Operation(summary = "取消收藏")
    public ApiResponse<Void> removeFavorite(@PathVariable Long templateId) {
        marketService.removeFavorite(templateId, UserContextHolder.getCurrentUserId());
        return ApiResponse.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "我的收藏")
    public ApiResponse<List<MarketTemplateVO>> getFavorites() {
        return ApiResponse.success(marketService.getFavorites(UserContextHolder.getCurrentUserId()));
    }
}
