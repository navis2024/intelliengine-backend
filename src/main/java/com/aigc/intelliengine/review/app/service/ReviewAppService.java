package com.aigc.intelliengine.review.app.service;

import com.aigc.intelliengine.review.domain.entity.ReviewComment;
import com.aigc.intelliengine.review.dto.CommentCreateRequest;
import com.aigc.intelliengine.review.infrastructure.dataobject.ReviewCommentDO;
import com.aigc.intelliengine.review.infrastructure.mapper.ReviewCommentMapper;
import com.aigc.intelliengine.review.vo.ReviewCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewAppService {
    private final ReviewCommentMapper commentMapper;
    
    @Transactional
    public ReviewCommentVO createComment(CommentCreateRequest request, Long userId) {
        ReviewCommentDO comment = new ReviewCommentDO();
        comment.setAssetId(request.getAssetId());
        comment.setProjectId(request.getProjectId());
        comment.setContent(request.getContent());
        comment.setCommentType(request.getCommentType() != null ? request.getCommentType() : "COMMENT");
        comment.setTimestamp(request.getTimestamp());
        comment.setPositionX(request.getPositionX());
        comment.setPositionY(request.getPositionY());
        comment.setStatus("OPEN");
        comment.setCreatedBy(userId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        comment.setIsDeleted(0);
        
        commentMapper.insert(comment);
        return toVO(comment);
    }
    
    public List<ReviewCommentVO> getCommentsByAsset(Long assetId) {
        return commentMapper.selectByAssetId(assetId).stream()
            .map(this::toVO).collect(Collectors.toList());
    }
    
    private ReviewCommentVO toVO(ReviewCommentDO comment) {
        ReviewCommentVO vo = new ReviewCommentVO();
        vo.setId(String.valueOf(comment.getId()));
        vo.setAssetId(String.valueOf(comment.getAssetId()));
        vo.setProjectId(String.valueOf(comment.getProjectId()));
        vo.setContent(comment.getContent());
        vo.setCommentType(comment.getCommentType());
        vo.setTimestamp(comment.getTimestamp());
        vo.setPositionX(comment.getPositionX());
        vo.setPositionY(comment.getPositionY());
        vo.setStatus(comment.getStatus());
        vo.setCreatedBy(String.valueOf(comment.getCreatedBy()));
        vo.setCreatedAt(comment.getCreatedAt());
        return vo;
    }
}
