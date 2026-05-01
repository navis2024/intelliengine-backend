package com.aigc.intelliengine.market.infrastructure.mapper;

import com.aigc.intelliengine.market.infrastructure.dataobject.MarketTemplateDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketTemplateMapper extends BaseMapper<MarketTemplateDO> {
    @Select("SELECT * FROM market_template WHERE status = 'PUBLISHED' AND is_deleted = 0 ORDER BY created_at DESC")
    List<MarketTemplateDO> selectPublished();
    
    @Select("SELECT * FROM market_template WHERE category_id = #{categoryId} AND status = 'PUBLISHED' AND is_deleted = 0 ORDER BY sales_count DESC")
    List<MarketTemplateDO> selectByCategory(@Param("categoryId") Long categoryId);
    
    @Select("SELECT * FROM market_template WHERE created_by = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<MarketTemplateDO> selectByCreator(@Param("userId") Long userId);
}
