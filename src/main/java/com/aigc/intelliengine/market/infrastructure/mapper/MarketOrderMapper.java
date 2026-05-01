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

    /**
     * 根据买家ID查询订单
     */
    default List<MarketOrderDO> selectByBuyerId(Long buyerId) {
        return selectByBuyer(buyerId);
    }

    /**
     * 根据买家ID和状态查询订单
     */
    @Select("SELECT * FROM market_order WHERE buyer_id = #{buyerId} AND status = #{status} ORDER BY created_at DESC")
    List<MarketOrderDO> selectByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") String status);

    @Select("SELECT * FROM market_order WHERE order_no = #{orderNo} LIMIT 1")
    MarketOrderDO selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT EXISTS(SELECT 1 FROM market_order WHERE order_no = #{orderNo})")
    boolean existsByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单
     */
    default int update(MarketOrderDO order) {
        return this.updateById(order);
    }
}
