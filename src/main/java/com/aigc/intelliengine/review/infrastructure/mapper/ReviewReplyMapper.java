package com.aigc.intelliengine.review.infrastructure.mapper;

import com.aigc.intelliengine.review.infrastructure.dataobject.ReviewReplyDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ReviewReplyMapper extends BaseMapper<ReviewReplyDO> {
    @Select("SELECT * FROM review_reply WHERE comment_id = #{commentId} AND is_deleted = 0 ORDER BY created_at")
    List<ReviewReplyDO> selectByCommentId(@Param("commentId") Long commentId);
}
