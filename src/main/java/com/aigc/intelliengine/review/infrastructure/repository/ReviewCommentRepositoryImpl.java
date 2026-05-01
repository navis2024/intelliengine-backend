package com.aigc.intelliengine.review.infrastructure.repository;

import com.aigc.intelliengine.review.infrastructure.dataobject.ReviewCommentDO;
import com.aigc.intelliengine.review.infrastructure.mapper.ReviewCommentMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ReviewCommentRepositoryImpl {
    private final ReviewCommentMapper commentMapper;
    
    public ReviewCommentRepositoryImpl(ReviewCommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }
    
    public ReviewCommentDO save(ReviewCommentDO comment) {
        commentMapper.insert(comment);
        return comment;
    }
    
    public ReviewCommentDO findById(Long id) {
        return commentMapper.selectById(id);
    }
    
    public List<ReviewCommentDO> findByAssetId(Long assetId) {
        return commentMapper.selectByAssetId(assetId);
    }
    
    public List<ReviewCommentDO> findByProjectId(Long projectId) {
        return commentMapper.selectByProjectId(projectId);
    }
    
    public boolean update(ReviewCommentDO comment) {
        return commentMapper.updateById(comment) > 0;
    }
    
    public boolean remove(Long id) {
        return commentMapper.deleteById(id) > 0;
    }
}
