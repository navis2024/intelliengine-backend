package com.aigc.intelliengine.review;

import com.aigc.intelliengine.review.model.entity.ReviewComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReviewCommentMapper extends BaseMapper<ReviewComment> {
    @Select("SELECT * FROM review_comment WHERE asset_id = #{assetId} AND is_deleted = 0 ORDER BY timestamp ASC")
    List<ReviewComment> selectByAsset(@Param("assetId") Long assetId);
}
