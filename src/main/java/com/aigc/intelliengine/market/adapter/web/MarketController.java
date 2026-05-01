package com.aigc.intelliengine.market.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.market.app.service.MarketAppService;
import com.aigc.intelliengine.market.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.vo.MarketTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
@Tag(name = "市场交易", description = "模板市场、订单管理")
public class MarketController {
    private final MarketAppService marketAppService;
    
    @PostMapping("/templates")
    @Operation(summary = "创建模板")
    public ApiResponse<MarketTemplateVO> createTemplate(@Valid @RequestBody MarketTemplateCreateRequest request) {
        Long userId = 1L;
        return ApiResponse.success(marketAppService.createTemplate(request, userId));
    }
    
    @GetMapping("/templates")
    @Operation(summary = "获取模板列表")
    public ApiResponse<List<MarketTemplateVO>> getTemplates() {
        return ApiResponse.success(marketAppService.getPublishedTemplates());
    }
    
    @GetMapping("/templates/{id}")
    @Operation(summary = "获取模板详情")
    public ApiResponse<MarketTemplateVO> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(marketAppService.getTemplateById(id));
    }
}
