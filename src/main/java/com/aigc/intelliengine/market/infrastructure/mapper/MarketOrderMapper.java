package com.aigc.intelliengine.market.infrastructure.mapper;

import com.aigc.intelliengine.market.infrastructure.dataobject.MarketOrderDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketOrderMapper extends BaseMapper<MarketOrderDO> {
    @Select("SELECT * FROM market_order WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<MarketOrderDO> selectByBuyer(@Param("buyerId") Long buyerId);
    
    @Select("SELECT * FROM market_order WHERE order_no = #{orderNo} LIMIT 1")
    MarketOrderDO selectByOrderNo(@Param("orderNo") String orderNo);
    
    @Select("SELECT EXISTS(SELECT 1 FROM market_order WHERE order_no = #{orderNo})")
    boolean existsByOrderNo(@Param("orderNo") String orderNo);
}
