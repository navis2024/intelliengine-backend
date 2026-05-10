package com.aigc.intelliengine.market;

import com.aigc.intelliengine.market.model.entity.MarketOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketOrderItemMapper extends BaseMapper<MarketOrderItem> {
    @Select("SELECT * FROM market_order_item WHERE order_id = #{orderId} AND is_deleted = 0")
    List<MarketOrderItem> selectByOrder(@Param("orderId") Long orderId);
}
