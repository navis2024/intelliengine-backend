package com.aigc.intelliengine.review.infrastructure.mapper;

import com.aigc.intelliengine.review.infrastructure.dataobject.ReviewCommentDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReviewCommentMapper extends BaseMapper<ReviewCommentDO> {
    @Select("SELECT * FROM review_comment WHERE asset_id = #{assetId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<ReviewCommentDO> selectByAssetId(@Param("assetId") Long assetId);
    
    @Select("SELECT * FROM review_comment WHERE project_id = #{projectId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<ReviewCommentDO> selectByProjectId(@Param("projectId") Long projectId);
    
    @Select("SELECT * FROM review_comment WHERE created_by = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<ReviewCommentDO> selectByCreator(@Param("userId") Long userId);
}
