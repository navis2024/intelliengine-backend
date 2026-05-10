package com.aigc.intelliengine.market;

import com.aigc.intelliengine.market.model.entity.MarketTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketTemplateMapper extends BaseMapper<MarketTemplate> {
    @Select("SELECT * FROM market_template WHERE status = 'PUBLISHED' AND is_deleted = 0 ORDER BY created_at DESC")
    List<MarketTemplate> selectPublished();

    @Select("SELECT * FROM market_template WHERE created_by = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<MarketTemplate> selectByCreator(@Param("userId") Long userId);
}
