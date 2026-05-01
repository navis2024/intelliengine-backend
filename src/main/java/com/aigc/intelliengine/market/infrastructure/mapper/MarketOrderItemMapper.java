package com.aigc.intelliengine.market.infrastructure.mapper;

import com.aigc.intelliengine.market.infrastructure.dataobject.MarketOrderItemDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 市场订单明细Mapper
 * <p>
 * 对应表: market_order_item
 *
 * @author 智擎开发团队
 * @version 1.0.0
 * @since 2024
 */
@Mapper
public interface MarketOrderItemMapper extends BaseMapper<MarketOrderItemDO> {

    /**
     * 根据订单ID查询明细
     *
     * @param orderId 订单ID
     * @return 明细列表
     */
    @Select("SELECT * FROM market_order_item WHERE order_id = #{orderId} AND is_deleted = 0")
    List<MarketOrderItemDO> selectByOrderId(@Param("orderId") Long orderId);
}
