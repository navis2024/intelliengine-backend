package com.aigc.intelliengine.market.app.service;

import com.aigc.intelliengine.common.result.PageResult;
import com.aigc.intelliengine.market.domain.entity.MarketOrder;
import com.aigc.intelliengine.market.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.infrastructure.dataobject.MarketOrderDO;
import com.aigc.intelliengine.market.infrastructure.dataobject.MarketOrderItemDO;
import com.aigc.intelliengine.market.infrastructure.dataobject.MarketTemplateDO;
import com.aigc.intelliengine.market.infrastructure.mapper.MarketOrderItemMapper;
import com.aigc.intelliengine.market.infrastructure.mapper.MarketOrderMapper;
import com.aigc.intelliengine.market.infrastructure.mapper.MarketTemplateMapper;
import com.aigc.intelliengine.market.vo.MarketOrderVO;
import com.aigc.intelliengine.market.vo.MarketTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketAppService {
    private final MarketTemplateMapper templateMapper;
    private final MarketOrderMapper orderMapper;
    private final MarketOrderItemMapper orderItemMapper;

    // ==================== 模板管理 ====================
    
    @Transactional
    public MarketTemplateVO createTemplate(MarketTemplateCreateRequest request, Long userId) {
        MarketTemplateDO template = new MarketTemplateDO();
        template.setAssetId(request.getAssetId());
        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setCategoryId(request.getCategoryId());
        template.setPrice(request.getPrice());
        template.setOriginalPrice(request.getOriginalPrice());
        template.setCurrency("CNY");
        template.setSalesCount(0);
        template.setViewCount(0);
        template.setRating(new BigDecimal("5.0"));
        template.setStatus("DRAFT");
        template.setCreatedBy(userId);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setIsDeleted(0);
        
        templateMapper.insert(template);
        return toVO(template);
    }
    
    public List<MarketTemplateVO> getPublishedTemplates() {
        return templateMapper.selectPublished().stream()
            .map(this::toVO).collect(Collectors.toList());
    }
    
    public MarketTemplateVO getTemplateById(Long id) {
        MarketTemplateDO template = templateMapper.selectById(id);
        return template != null ? toVO(template) : null;
    }
    
    private MarketTemplateVO toVO(MarketTemplateDO template) {
        MarketTemplateVO vo = new MarketTemplateVO();
        vo.setId(String.valueOf(template.getId()));
        vo.setAssetId(String.valueOf(template.getAssetId()));
        vo.setTitle(template.getTitle());
        vo.setDescription(template.getDescription());
        vo.setPrice(template.getPrice());
        vo.setOriginalPrice(template.getOriginalPrice());
        vo.setSalesCount(template.getSalesCount());
        vo.setViewCount(template.getViewCount());
        vo.setRating(template.getRating());
        vo.setStatus(template.getStatus());
        vo.setCreateTime(template.getCreatedAt());
        return vo;
    }

    // ==================== 订单管理 ====================

    /**
     * 创建订单
     *
     * @param templateId 模板ID
     * @param userId     用户ID
     * @return 订单VO
     */
    @Transactional
    public MarketOrderVO createOrder(Long templateId, Long userId) {
        MarketTemplateDO template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        // 创建订单主表
        MarketOrderDO order = new MarketOrderDO();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(template.getCreatedBy());
        order.setTotalAmount(template.getPrice());
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayableAmount(template.getPrice());
        order.setPayMethod(null);
        order.setPayTime(null);
        order.setStatus("PENDING_PAID");
        order.setPayTimeoutAt(LocalDateTime.now().plusMinutes(30));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setIsDeleted(0);
        orderMapper.insert(order);

        // 创建订单明细
        MarketOrderItemDO item = new MarketOrderItemDO();
        item.setOrderId(order.getId());
        item.setTemplateId(templateId);
        item.setTemplateTitle(template.getTitle());
        item.setTemplateThumbnail(template.getThumbnailUrl());
        item.setUnitPrice(template.getPrice());
        item.setQuantity(1);
        item.setSubtotal(template.getPrice());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setIsDeleted(0);
        orderItemMapper.insert(item);

        return toOrderVO(order, template);
    }

    /**
     * 获取用户订单列表
     *
     * @param userId   用户ID
     * @param status   订单状态
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public PageResult<MarketOrderVO> getUserOrders(Long userId, String status, Integer pageNum, Integer pageSize) {
        // 这里简化处理，实际应使用分页查询
        List<MarketOrderDO> orders;
        if (status != null) {
            orders = orderMapper.selectByBuyerIdAndStatus(userId, status);
        } else {
            orders = orderMapper.selectByBuyerId(userId);
        }

        List<MarketOrderVO> voList = orders.stream()
                .map(order -> toOrderVO(order, null))
                .collect(Collectors.toList());

        return new PageResult<>(voList, (long) voList.size(), pageNum, pageSize);
    }

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单VO
     */
    public MarketOrderVO getOrderById(Long orderId) {
        MarketOrderDO order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        return toOrderVO(order, null);
    }

    /**
     * 支付订单
     *
     * @param orderId   订单ID
     * @param userId    用户ID
     * @param payMethod 支付方式
     * @return 订单VO
     */
    @Transactional
    public MarketOrderVO payOrder(Long orderId, Long userId, String payMethod) {
        MarketOrderDO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"PENDING_PAID".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus("PAID");
        order.setPayMethod(payMethod);
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);

        return toOrderVO(order, null);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        MarketOrderDO order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"PENDING_PAID".equals(order.getStatus())) {
            throw new RuntimeException("只有待支付订单可以取消");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.update(order);
    }

    /**
     * 生成订单号
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        return "ORD_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 转换为OrderVO
     *
     * @param order    订单DO
     * @param template 模板DO（可为null）
     * @return OrderVO
     */
    private MarketOrderVO toOrderVO(MarketOrderDO order, MarketTemplateDO template) {
        MarketOrderVO vo = new MarketOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setTotalAmount(order.getPayableAmount());
        vo.setPayMethod(order.getPayMethod());
        vo.setPayTime(order.getPayTime());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());

        // 状态中文映射
        switch (order.getStatus()) {
            case "PENDING_PAID":
                vo.setStatusText("待支付");
                break;
            case "PAID":
                vo.setStatusText("已支付");
                break;
            case "COMPLETED":
                vo.setStatusText("已完成");
                break;
            case "CANCELLED":
                vo.setStatusText("已取消");
                break;
            default:
                vo.setStatusText(order.getStatus());
        }

        // 填充模板信息
        if (template != null) {
            vo.setTemplateId(template.getId());
            vo.setTemplateTitle(template.getTitle());
            vo.setTemplateThumbnail(template.getThumbnailUrl());
        }

        return vo;
    }
}
