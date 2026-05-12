package com.aigc.intelliengine.review;

import com.aigc.intelliengine.common.exception.BusinessException;
import com.aigc.intelliengine.common.security.MembershipValidator;
import com.aigc.intelliengine.review.model.dto.CommentCreateRequest;
import com.aigc.intelliengine.review.model.dto.ReplyCreateRequest;
import com.aigc.intelliengine.review.model.entity.ReviewComment;
import com.aigc.intelliengine.review.model.entity.ReviewReply;
import com.aigc.intelliengine.review.model.vo.ReviewCommentVO;
import com.aigc.intelliengine.review.model.vo.ReviewReplyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewCommentMapper commentMapper;
    private final ReviewReplyMapper replyMapper;
    private final MembershipValidator validator;

    @Transactional
    public ReviewCommentVO createComment(CommentCreateRequest request, Long userId) {
        validator.requireAssetAccess(request.getAssetId(), userId);
        ReviewComment comment = new ReviewComment();
        comment.setAssetId(request.getAssetId());
        comment.setProjectId(request.getProjectId());
        comment.setContent(request.getContent());
        comment.setTimestamp(request.getTimestamp());
        comment.setPositionX(request.getPositionX());
        comment.setPositionY(request.getPositionY());
        comment.setCommentType("COMMENT");
        comment.setStatus("PENDING");
        comment.setCreatedBy(userId);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return toCommentVO(comment);
    }

    public List<ReviewCommentVO> getCommentsByAsset(Long assetId, Long userId) {
        validator.requireAssetAccess(assetId, userId);
        List<ReviewComment> comments = commentMapper.selectByAsset(assetId);
        return comments.stream().map(c -> {
            ReviewCommentVO vo = toCommentVO(c);
            List<ReviewReply> replies = replyMapper.selectByComment(c.getId());
            vo.setReplies(replies.stream().map(this::toReplyVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateCommentStatus(Long commentId, String status, Long userId) {
        ReviewComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!comment.getCreatedBy().equals(userId))
            throw new BusinessException("只有评论作者可以修改");
        comment.setStatus(status);
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        ReviewComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!comment.getCreatedBy().equals(userId))
            throw new BusinessException("只有评论作者可以删除");
        commentMapper.deleteById(commentId);
    }

    @Transactional
    public ReviewReplyVO createReply(Long commentId, ReplyCreateRequest request, Long userId) {
        ReviewReply reply = new ReviewReply();
        reply.setCommentId(commentId);
        reply.setContent(request.getContent());
        reply.setCreatedBy(userId);
        reply.setCreatedAt(LocalDateTime.now());
        replyMapper.insert(reply);
        return toReplyVO(reply);
    }

    @Transactional
    public ReviewReplyVO updateReply(Long replyId, String content, Long userId) {
        ReviewReply reply = replyMapper.selectById(replyId);
        if (reply == null) throw new BusinessException("回复不存在");
        if (!reply.getCreatedBy().equals(userId))
            throw new BusinessException("只有回复作者可以修改");
        reply.setContent(content);
        replyMapper.updateById(reply);
        return toReplyVO(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        ReviewReply reply = replyMapper.selectById(replyId);
        if (reply == null) throw new BusinessException("回复不存在");
        if (!reply.getCreatedBy().equals(userId))
            throw new BusinessException("只有回复作者可以删除");
        replyMapper.deleteById(replyId);
    }

    private ReviewCommentVO toCommentVO(ReviewComment c) {
        if (c == null) return null;
        ReviewCommentVO vo = new ReviewCommentVO();
        vo.setId(String.valueOf(c.getId()));
        vo.setAssetId(String.valueOf(c.getAssetId()));
        vo.setProjectId(String.valueOf(c.getProjectId()));
        vo.setContent(c.getContent());
        vo.setCommentType(c.getCommentType());
        vo.setTimestamp(c.getTimestamp());
        vo.setPositionX(c.getPositionX());
        vo.setPositionY(c.getPositionY());
        vo.setStatus(c.getStatus());
        vo.setCreatedBy(String.valueOf(c.getCreatedBy()));
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private ReviewReplyVO toReplyVO(ReviewReply r) {
        if (r == null) return null;
        ReviewReplyVO vo = new ReviewReplyVO();
        vo.setId(String.valueOf(r.getId()));
        vo.setCommentId(String.valueOf(r.getCommentId()));
        vo.setContent(r.getContent());
        vo.setCreatedBy(String.valueOf(r.getCreatedBy()));
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
