package com.aigc.intelliengine.market;

import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.redis.MultiLevelCacheService;
import com.aigc.intelliengine.common.redis.RedisBloomFilter;
import com.aigc.intelliengine.market.model.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.model.dto.OrderCreateRequest;
import com.aigc.intelliengine.market.model.entity.MarketFavorite;
import com.aigc.intelliengine.market.model.entity.MarketOrder;
import com.aigc.intelliengine.market.model.entity.MarketOrderItem;
import com.aigc.intelliengine.market.model.entity.MarketTemplate;
import com.aigc.intelliengine.market.model.vo.MarketOrderVO;
import com.aigc.intelliengine.market.model.vo.MarketTemplateVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketTemplateMapper templateMapper;
    private final MarketOrderMapper orderMapper;
    private final MarketOrderItemMapper orderItemMapper;
    private final MarketFavoriteMapper favoriteMapper;
    private final MultiLevelCacheService cacheService;
    private final RedisBloomFilter bloomFilter;

    @SuppressWarnings("unchecked")
    public PageResult<MarketTemplateVO> listTemplates(String keyword, String sort, Integer pageNum, Integer pageSize) {
        String cacheKey = String.format("market:templates:%s:%s:%d:%d",
                keyword != null ? keyword : "*", sort != null ? sort : "newest", pageNum, pageSize);
        return cacheService.getOrLoad(cacheKey, () -> loadTemplates(keyword, sort, pageNum, pageSize), 5);
    }

    private PageResult<MarketTemplateVO> loadTemplates(String keyword, String sort, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<MarketTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketTemplate::getStatus, "PUBLISHED");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(MarketTemplate::getTitle, keyword);
        }
        if ("sales".equals(sort)) wrapper.orderByDesc(MarketTemplate::getSalesCount);
        else if ("rating".equals(sort)) wrapper.orderByDesc(MarketTemplate::getRating);
        else if ("price".equals(sort)) wrapper.orderByAsc(MarketTemplate::getPrice);
        else wrapper.orderByDesc(MarketTemplate::getCreatedAt);
        Page<MarketTemplate> page = templateMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toTemplateVO).toList(), page.getTotal(), pageNum, pageSize);
    }

    public MarketTemplateVO getTemplate(Long id) {
        MarketTemplate t = templateMapper.selectById(id);
        if (t == null) throw new BusinessException("模板不存在");
        return toTemplateVO(t);
    }

    @Transactional
    public MarketTemplateVO createTemplate(MarketTemplateCreateRequest request, Long userId) {
        MarketTemplate t = new MarketTemplate();
        t.setAssetId(request.getAssetId());
        t.setTitle(request.getTitle());
        t.setDescription(request.getDescription());
        t.setPrice(request.getPrice());
        t.setOriginalPrice(request.getOriginalPrice());
        t.setStatus("DRAFT");
        t.setSalesCount(0);
        t.setViewCount(0);
        t.setRating(BigDecimal.ZERO);
        t.setCreatedBy(userId);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        templateMapper.insert(t);
        evictTemplateCache();
        return toTemplateVO(t);
    }

    @Transactional
    public MarketOrderVO createOrder(OrderCreateRequest request, Long buyerId) {
        BigDecimal total = BigDecimal.ZERO;
        MarketOrder order = new MarketOrder();
        order.setOrderNo("ORD_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setBuyerId(buyerId);
        order.setStatus("PENDING");
        order.setCurrency("CNY");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        for (OrderCreateRequest.OrderItem itemReq : request.getItems()) {
            // Bloom filter: fast duplicate purchase check
            String dupKey = buyerId + ":" + itemReq.getTemplateId();
            if (!bloomFilter.checkAndAdd("purchase", dupKey)) {
                throw new BusinessException(429, "请勿重复购买同一模板，订单处理中");
            }
            MarketTemplate template = templateMapper.selectById(itemReq.getTemplateId());
            if (template == null) throw new BusinessException("模板不存在: " + itemReq.getTemplateId());
            MarketOrderItem item = new MarketOrderItem();
            item.setOrderId(order.getId());
            item.setTemplateId(template.getId());
            item.setTemplateTitle(template.getTitle());
            item.setTemplatePrice(template.getPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setSubtotal(template.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            orderItemMapper.insert(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
        order.setPayAmount(total);
        orderMapper.updateById(order);
        return toOrderVO(order);
    }

    public PageResult<MarketOrderVO> getMyOrders(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<MarketOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketOrder::getBuyerId, userId).orderByDesc(MarketOrder::getCreatedAt);
        Page<MarketOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords().stream().map(this::toOrderVO).toList(), page.getTotal(), pageNum, pageSize);
    }

    public MarketOrderVO getOrder(Long id, Long userId) {
        MarketOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(userId))
            throw new BusinessException("无权查看该订单");
        return toOrderVO(order);
    }

    @Transactional
    public MarketTemplateVO updateTemplate(Long id, MarketTemplateCreateRequest request) {
        MarketTemplate t = templateMapper.selectById(id);
        if (t == null) throw new BusinessException("模板不存在");
        if (request.getTitle() != null) t.setTitle(request.getTitle());
        if (request.getDescription() != null) t.setDescription(request.getDescription());
        if (request.getPrice() != null) t.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) t.setOriginalPrice(request.getOriginalPrice());
        t.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(t);
        evictTemplateCache();
        return toTemplateVO(t);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        MarketTemplate t = templateMapper.selectById(id);
        if (t != null) templateMapper.deleteById(id);
        evictTemplateCache();
    }

    @Transactional
    public void cancelOrder(Long id) {
        MarketOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    public List<MarketTemplateVO> getFavorites(Long userId) {
        return favoriteMapper.selectFavoriteTemplates(userId).stream()
                .map(this::toTemplateVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(Long templateId, Long userId) {
        if (templateMapper.selectById(templateId) == null) {
            throw new BusinessException("模板不存在");
        }
        if (favoriteMapper.countByUserAndTemplate(userId, templateId) > 0) {
            return;
        }
        MarketFavorite favorite = new MarketFavorite();
        favorite.setUserId(userId);
        favorite.setTemplateId(templateId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);
    }

    @Transactional
    public void removeFavorite(Long templateId, Long userId) {
        LambdaQueryWrapper<MarketFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MarketFavorite::getUserId, userId)
                .eq(MarketFavorite::getTemplateId, templateId);
        favoriteMapper.delete(wrapper);
    }

    private void evictTemplateCache() {
        // Delayed double-delete: first delete now, second after 800ms to catch stale concurrent reads
        cacheService.delayedDoubleDelete("market:templates:*", 800);
    }

    private MarketTemplateVO toTemplateVO(MarketTemplate t) {
        if (t == null) return null;
        MarketTemplateVO vo = new MarketTemplateVO();
        vo.setId(String.valueOf(t.getId()));
        vo.setAssetId(String.valueOf(t.getAssetId()));
        vo.setTitle(t.getTitle());
        vo.setDescription(t.getDescription());
        vo.setPrice(t.getPrice());
        vo.setOriginalPrice(t.getOriginalPrice());
        vo.setSalesCount(t.getSalesCount());
        vo.setViewCount(t.getViewCount());
        vo.setRating(t.getRating());
        vo.setStatus(t.getStatus());
        vo.setCreatedBy(String.valueOf(t.getCreatedBy()));
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }

    private MarketOrderVO toOrderVO(MarketOrder order) {
        if (order == null) return null;
        MarketOrderVO vo = new MarketOrderVO();
        vo.setId(String.valueOf(order.getId()));
        vo.setOrderNo(order.getOrderNo());
        vo.setBuyerId(String.valueOf(order.getBuyerId()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        List<MarketOrderItem> items = orderItemMapper.selectByOrder(order.getId());
        vo.setItems(items.stream().map(i -> {
            MarketOrderVO.OrderItemVO iv = new MarketOrderVO.OrderItemVO();
            iv.setTemplateId(String.valueOf(i.getTemplateId()));
            iv.setTemplateTitle(i.getTemplateTitle());
            iv.setTemplatePrice(i.getTemplatePrice());
            iv.setQuantity(i.getQuantity());
            iv.setSubtotal(i.getSubtotal());
            return iv;
        }).collect(Collectors.toList()));
        return vo;
    }
}
