package com.aigc.intelliengine.market;

import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.model.PageResult;
import com.aigc.intelliengine.common.redis.MultiLevelCacheService;
import com.aigc.intelliengine.common.redis.RedisBloomFilter;
import com.aigc.intelliengine.market.model.dto.MarketTemplateCreateRequest;
import com.aigc.intelliengine.market.model.dto.OrderCreateRequest;
import com.aigc.intelliengine.market.model.entity.MarketOrder;
import com.aigc.intelliengine.market.model.entity.MarketTemplate;
import com.aigc.intelliengine.market.model.vo.MarketOrderVO;
import com.aigc.intelliengine.market.model.vo.MarketTemplateVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock private MarketTemplateMapper templateMapper;
    @Mock private MarketOrderMapper orderMapper;
    @Mock private MarketOrderItemMapper orderItemMapper;
    @Mock private MarketFavoriteMapper favoriteMapper;
    @Mock private MultiLevelCacheService cacheService;
    @Mock private RedisBloomFilter bloomFilter;
    @InjectMocks private MarketService marketService;

    @SuppressWarnings("unchecked")
    @Test
    void listTemplates_shouldUseCache() {
        Page<MarketTemplate> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(template("Pro Template", BigDecimal.TEN)));
        when(templateMapper.selectPage(any(), any())).thenReturn(page);
        when(cacheService.getOrLoad(anyString(), any(), anyLong()))
                .thenAnswer(inv -> ((java.util.function.Supplier<?>) inv.getArgument(1)).get());
        PageResult<MarketTemplateVO> r = marketService.listTemplates(null, null, 1, 10);
        assertEquals(1L, r.getTotal());
    }

    @Test
    void getTemplate_success() {
        when(templateMapper.selectById(1L)).thenReturn(template("T1", BigDecimal.ONE));
        assertEquals("T1", marketService.getTemplate(1L).getTitle());
    }

    @Test
    void getTemplate_notFound_shouldThrow() {
        when(templateMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> marketService.getTemplate(999L));
    }

    @Test
    void createTemplate_success() {
        var req = new MarketTemplateCreateRequest();
        req.setTitle("New"); req.setPrice(BigDecimal.valueOf(9.9)); req.setDescription("d");
        MarketTemplateVO vo = marketService.createTemplate(req, 1L);
        assertEquals("New", vo.getTitle());
    }

    @Test
    void createOrder_success() {
        var item = new OrderCreateRequest.OrderItem(); item.setTemplateId(1L); item.setQuantity(2);
        var req = new OrderCreateRequest(); req.setItems(List.of(item));
        when(bloomFilter.checkAndAdd(anyString(), anyString())).thenReturn(true);
        when(templateMapper.selectById(1L)).thenReturn(template("T", BigDecimal.valueOf(99)));
        MarketOrderVO vo = marketService.createOrder(req, 1L);
        assertTrue(vo.getOrderNo().startsWith("ORD_"));
    }

    @Test
    void createOrder_templateNotFound_shouldThrow() {
        var item = new OrderCreateRequest.OrderItem(); item.setTemplateId(999L); item.setQuantity(1);
        var req = new OrderCreateRequest(); req.setItems(List.of(item));
        when(bloomFilter.checkAndAdd(anyString(), anyString())).thenReturn(true);
        when(templateMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> marketService.createOrder(req, 1L));
    }

    @Test
    void getMyOrders_shouldReturnPaged() {
        MarketOrder o = new MarketOrder(); o.setId(1L); o.setOrderNo("X"); o.setBuyerId(1L); o.setTotalAmount(BigDecimal.TEN);
        Page<MarketOrder> page = new Page<>(1, 10, 1); page.setRecords(List.of(o));
        when(orderMapper.selectPage(any(), any())).thenReturn(page);
        when(orderItemMapper.selectByOrder(1L)).thenReturn(List.of());
        assertEquals(1L, marketService.getMyOrders(1L, 1, 10).getTotal());
    }

    @Test
    void getOrder_notBuyer_shouldThrow() {
        MarketOrder o = new MarketOrder(); o.setId(1L); o.setBuyerId(99L);
        when(orderMapper.selectById(1L)).thenReturn(o);
        assertThrows(BusinessException.class, () -> marketService.getOrder(1L, 1L));
    }

    @Test
    void cancelOrder_success() {
        MarketOrder o = new MarketOrder(); o.setId(1L); o.setStatus("PENDING");
        when(orderMapper.selectById(1L)).thenReturn(o);
        marketService.cancelOrder(1L);
        assertEquals("CANCELLED", o.getStatus());
    }

    @Test
    void addFavorite_success() {
        when(templateMapper.selectById(1L)).thenReturn(template("T", BigDecimal.ONE));
        when(favoriteMapper.countByUserAndTemplate(1L, 1L)).thenReturn(0);
        assertDoesNotThrow(() -> marketService.addFavorite(1L, 1L));
    }

    @Test
    void addFavorite_duplicate_shouldBeIdempotent() {
        when(templateMapper.selectById(1L)).thenReturn(template("T", BigDecimal.ONE));
        when(favoriteMapper.countByUserAndTemplate(1L, 1L)).thenReturn(1);
        assertDoesNotThrow(() -> marketService.addFavorite(1L, 1L));
    }

    @Test
    void addFavorite_templateNotFound_shouldThrow() {
        when(templateMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> marketService.addFavorite(999L, 1L));
    }

    @Test
    void removeFavorite_success() {
        assertDoesNotThrow(() -> marketService.removeFavorite(1L, 1L));
    }

    @Test
    void getFavorites_shouldReturnList() {
        when(favoriteMapper.selectFavoriteTemplates(1L)).thenReturn(List.of(template("Fav", BigDecimal.ONE)));
        assertEquals(1, marketService.getFavorites(1L).size());
    }

    private MarketTemplate template(String title, BigDecimal price) {
        MarketTemplate t = new MarketTemplate();
        t.setId(1L); t.setTitle(title); t.setDescription("d"); t.setPrice(price);
        t.setOriginalPrice(price.multiply(BigDecimal.valueOf(2))); t.setSalesCount(0);
        t.setViewCount(10); t.setRating(BigDecimal.valueOf(4.5)); t.setStatus("PUBLISHED");
        t.setCreatedBy(1L); t.setCreatedAt(java.time.LocalDateTime.now());
        return t;
    }
}
