package com.aigc.intelliengine.market.adapter.web;

import com.aigc.intelliengine.common.result.ApiResponse;
import com.aigc.intelliengine.common.result.PageResult;
import com.aigc.intelliengine.market.app.service.MarketAppService;
import com.aigc.intelliengine.market.vo.MarketOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 市场订单控制器
 * <p>
 * 提供订单创建、查询、支付等功能
 * 对应表: market_order, market_order_item
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@RestController
@RequestMapping("/v1/market/orders")
@RequiredArgsConstructor
@Tag(name = "市场订单", description = "模板订单管理 - 下单/支付/查询")
public class MarketOrderController {

    private final MarketAppService marketAppService;

    /**
     * 创建订单
     * <p>
     * 根据模板ID创建订单，初始状态为待支付
     *
     * @param templateId  模板ID
     * @param httpRequest HTTP请求
     * @return 订单信息
     */
    @PostMapping
    @Operation(
            summary = "创建订单",
            description = "购买模板创建订单，初始状态为待支付"
    )
    public ApiResponse<MarketOrderVO> createOrder(
            @Parameter(description = "模板ID", required = true, example = "1")
            @RequestParam Long templateId,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(marketAppService.createOrder(templateId, userId));
    }

    /**
     * 获取当前用户的订单列表
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param status     订单状态筛选（可选）
     * @param httpRequest HTTP请求
     * @return 订单分页列表
     */
    @GetMapping("/my")
    @Operation(
            summary = "我的订单",
            description = "获取当前登录用户的订单列表"
    )
    public ApiResponse<PageResult<MarketOrderVO>> getMyOrders(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,

            @Parameter(description = "订单状态(PENDING_PAID/PAID/COMPLETED/CANCELLED)")
            @RequestParam(required = false) String status,

            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(marketAppService.getUserOrders(userId, status, pageNum, pageSize));
    }

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单详细信息
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "订单详情",
            description = "根据订单ID获取详细信息"
    )
    public ApiResponse<MarketOrderVO> getOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long id
    ) {
        return ApiResponse.success(marketAppService.getOrderById(id));
    }

    /**
     * 支付订单
     * <p>
     * 模拟支付流程，支付成功后更新订单状态为已支付
     *
     * @param id          订单ID
     * @param payMethod   支付方式
     * @param httpRequest HTTP请求
     * @return 支付结果
     */
    @PostMapping("/{id}/pay")
    @Operation(
            summary = "支付订单",
            description = "支付订单，支持模拟支付(ALIPAY/WECHAPAY)"
    )
    public ApiResponse<MarketOrderVO> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long id,

            @Parameter(description = "支付方式", example = "ALIPAY")
            @RequestParam(defaultValue = "ALIPAY") String payMethod,

            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        return ApiResponse.success(marketAppService.payOrder(id, userId, payMethod));
    }

    /**
     * 取消订单
     *
     * @param id          订单ID
     * @param httpRequest HTTP请求
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "取消订单",
            description = "取消待支付状态的订单"
    )
    public ApiResponse<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        marketAppService.cancelOrder(id, userId);
        return ApiResponse.success("取消成功", null);
    }
}
