package com.peekmoment.backend.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;
import com.peekmoment.backend.dto.CommentRequest;
import com.peekmoment.backend.dto.CommentVO;
import com.peekmoment.backend.dto.RatingRequest;
import com.peekmoment.backend.entity.Comment;
import com.peekmoment.backend.entity.Content;
import com.peekmoment.backend.entity.Rating;
import com.peekmoment.backend.entity.User;
import com.peekmoment.backend.mapper.CommentMapper;
import com.peekmoment.backend.mapper.ContentMapper;
import com.peekmoment.backend.mapper.RatingMapper;
import com.peekmoment.backend.mapper.UserMapper;
import com.peekmoment.backend.util.TimeUtil;

import lombok.RequiredArgsConstructor;

/**
 * 互动模块：评分（覆盖式）、评论（一级 + 二级回复）。
 */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final ContentMapper contentMapper;
    private final RatingMapper ratingMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Transactional
    public Integer rate(Long userId, Long contentId, RatingRequest req) {
        requirePublished(contentId);
        Rating existing = ratingMapper.selectOne(new LambdaQueryWrapper<Rating>()
                .eq(Rating::getContentId, contentId)
                .eq(Rating::getUserId, userId));
        if (existing == null) {
            Rating rating = new Rating();
            rating.setContentId(contentId);
            rating.setUserId(userId);
            rating.setScore(req.score());
            ratingMapper.insert(rating);
        } else {
            existing.setScore(req.score());
            ratingMapper.updateById(existing);
        }
        ratingMapper.recalcRating(contentId);
        Content content = contentMapper.selectById(contentId);
        return content == null ? null : req.score();
    }

    public List<CommentVO> comments(Long contentId) {
        requirePublished(contentId);
        List<Comment> all = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL)
                .orderByAsc(Comment::getCreatedAt));
        Map<Long, User> userMap = userMapOf(all.stream().map(Comment::getUserId).toList());
        Map<Long, List<Comment>> replies = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> toVO(c, userMap, replies.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    @Transactional
    public CommentVO addComment(Long userId, Long contentId, CommentRequest req) {
        requirePublished(contentId);
        if (req.parentId() != null) {
            Comment parent = commentMapper.selectById(req.parentId());
            if (parent == null || !parent.getContentId().equals(contentId)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "回复的评论不存在");
            }
        }
        Comment comment = new Comment();
        comment.setContentId(contentId);
        comment.setUserId(userId);
        comment.setParentId(req.parentId());
        comment.setBody(req.body().trim());
        comment.setStatus(Comment.STATUS_NORMAL);
        comment.setDeleted(0);
        commentMapper.insert(comment);
        commentMapper.incrCommentCount(contentId);

        // 回读获取数据库生成的时间
        Comment saved = commentMapper.selectById(comment.getId());
        User author = userMapper.selectById(userId);
        return new CommentVO(saved.getId(), userId,
                author == null ? "未知用户" : author.getNickname(),
                saved.getBody(), TimeUtil.toMillis(saved.getCreatedAt()), List.of());
    }

    private void requirePublished(Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null || content.getStatus() != Content.STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
    }

    private Map<Long, User> userMapOf(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private CommentVO toVO(Comment c, Map<Long, User> userMap, List<Comment> replies) {
        User author = userMap.get(c.getUserId());
        List<CommentVO> replyVOs = replies.stream()
                .map(r -> toVO(r, userMap, List.of()))
                .toList();
        return new CommentVO(c.getId(), c.getUserId(),
                author == null ? "未知用户" : author.getNickname(),
                c.getBody(), TimeUtil.toMillis(c.getCreatedAt()), replyVOs);
    }
}
