package com.peekmoment.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;
import com.peekmoment.backend.dto.ContentVO;
import com.peekmoment.backend.dto.NameCountRow;
import com.peekmoment.backend.dto.PageVO;
import com.peekmoment.backend.dto.ScoreRow;
import com.peekmoment.backend.dto.SummaryVO;
import com.peekmoment.backend.dto.TrendRow;
import com.peekmoment.backend.dto.UserVO;
import com.peekmoment.backend.entity.Admin;
import com.peekmoment.backend.entity.Content;
import com.peekmoment.backend.entity.OperationLog;
import com.peekmoment.backend.entity.User;
import com.peekmoment.backend.mapper.AdminMapper;
import com.peekmoment.backend.mapper.ContentMapper;
import com.peekmoment.backend.mapper.OperationLogMapper;
import com.peekmoment.backend.mapper.StatsMapper;
import com.peekmoment.backend.mapper.UserMapper;
import com.peekmoment.backend.util.TimeUtil;

import lombok.RequiredArgsConstructor;

/**
 * 管理端：用户管理、内容审核、统计，写操作记操作日志。
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final ContentMapper contentMapper;
    private final StatsMapper statsMapper;
    private final OperationLogMapper operationLogMapper;

    public PageVO<UserVO> users(long page, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public UserVO userDetail(Long userId) {
        return toVO(requireUser(userId));
    }

    public UserVO updateUserStatus(Long userId, Integer status) {
        User user = requireUser(userId);
        user.setStatus(status);
        userMapper.updateById(user);
        log("update_user_status", "user", userId, "status=" + status);
        return toVO(user);
    }

    public void deleteUser(Long userId) {
        requireUser(userId);
        userMapper.deleteById(userId);
        log("delete_user", "user", userId, null);
    }

    public PageVO<ContentVO> contents(long page, long size, Integer status) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Content::getStatus, status);
        }
        wrapper.orderByDesc(Content::getCreatedAt);
        Page<Content> result = contentMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContentVO> vos = result.getRecords().stream()
                .map(c -> new ContentVO(c.getId(), c.getUserId(), null, null,
                        c.getTitle(), c.getBody(), List.of(), List.of(),
                        TimeUtil.toMillis(c.getCreatedAt()), c.getRatingAvg(),
                        c.getRatingCount(), c.getCommentCount(), c.getStatus(), null))
                .toList();
        return new PageVO<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ContentVO updateContentStatus(Long contentId, Integer status) {
        Content content = requireContent(contentId);
        content.setStatus(status);
        contentMapper.updateById(content);
        log("update_content_status", "content", contentId, "status=" + status);
        return new ContentVO(content.getId(), content.getUserId(), null, null,
                content.getTitle(), content.getBody(), List.of(), List.of(),
                TimeUtil.toMillis(content.getCreatedAt()), content.getRatingAvg(),
                content.getRatingCount(), content.getCommentCount(), content.getStatus(), null);
    }

    public void deleteContent(Long contentId) {
        requireContent(contentId);
        contentMapper.deleteById(contentId);
        log("delete_content", "content", contentId, null);
    }

    public SummaryVO summary() {
        long users = userMapper.selectCount(null);
        long contents = contentMapper.selectCount(null);
        long pending = contentMapper.selectCount(new LambdaQueryWrapper<Content>()
                .eq(Content::getStatus, Content.STATUS_PENDING));
        return new SummaryVO(users, contents, pending);
    }

    public List<TrendRow> trend(int days) {
        return statsMapper.trendByDay(LocalDate.now().minusDays(days).atStartOfDay());
    }

    public List<NameCountRow> tagDistribution() {
        return statsMapper.tagDistribution();
    }

    public List<ScoreRow> scoreDistribution() {
        return statsMapper.scoreDistribution();
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Content requireContent(Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        return content;
    }

    private void log(String action, String targetType, Long targetId, String detail) {
        OperationLog entry = new OperationLog();
        entry.setAdminId(UserContext.currentUserId());
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setDetail(detail);
        operationLogMapper.insert(entry);
    }

    private UserVO toVO(User user) {
        return new UserVO(user.getId(), user.getUsername(), user.getNickname(),
                user.getAvatarUrl(), user.getStatus(), TimeUtil.toMillis(user.getCreatedAt()));
    }
}
