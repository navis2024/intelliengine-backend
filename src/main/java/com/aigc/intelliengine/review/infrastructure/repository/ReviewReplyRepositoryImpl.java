package com.aigc.intelliengine.review.infrastructure.repository;

import com.aigc.intelliengine.review.infrastructure.dataobject.ReviewReplyDO;
import com.aigc.intelliengine.review.infrastructure.mapper.ReviewReplyMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ReviewReplyRepositoryImpl {
    private final ReviewReplyMapper replyMapper;
    
    public ReviewReplyRepositoryImpl(ReviewReplyMapper replyMapper) {
        this.replyMapper = replyMapper;
    }
    
    public ReviewReplyDO save(ReviewReplyDO reply) {
        replyMapper.insert(reply);
        return reply;
    }
    
    public List<ReviewReplyDO> findByCommentId(Long commentId) {
        return replyMapper.selectByCommentId(commentId);
    }
    
    public boolean remove(Long id) {
        return replyMapper.deleteById(id) > 0;
    }
}
