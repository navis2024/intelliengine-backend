package com.aigc.intelliengine.market;

import com.aigc.intelliengine.market.model.entity.MarketOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketOrderMapper extends BaseMapper<MarketOrder> {
    @Select("SELECT * FROM market_order WHERE buyer_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<MarketOrder> selectByBuyer(@Param("userId") Long userId);

    @Select("SELECT * FROM market_order WHERE order_no = #{orderNo} AND is_deleted = 0 LIMIT 1")
    MarketOrder selectByOrderNo(@Param("orderNo") String orderNo);
}
