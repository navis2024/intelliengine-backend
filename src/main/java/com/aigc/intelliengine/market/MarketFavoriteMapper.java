package com.aigc.intelliengine.market;

import com.aigc.intelliengine.market.model.entity.MarketFavorite;
import com.aigc.intelliengine.market.model.entity.MarketTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MarketFavoriteMapper extends BaseMapper<MarketFavorite> {

    @Select("SELECT t.* FROM market_template t " +
            "INNER JOIN market_favorite f ON f.template_id = t.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<MarketTemplate> selectFavoriteTemplates(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM market_favorite WHERE user_id = #{userId} AND template_id = #{templateId}")
    int countByUserAndTemplate(@Param("userId") Long userId, @Param("templateId") Long templateId);
}
