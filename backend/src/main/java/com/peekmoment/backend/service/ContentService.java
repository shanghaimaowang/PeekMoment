package com.peekmoment.backend.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.BusinessException;
import com.peekmoment.backend.common.ResultCode;
import com.peekmoment.backend.dto.ContentTagRow;
import com.peekmoment.backend.dto.ContentVO;
import com.peekmoment.backend.dto.PageVO;
import com.peekmoment.backend.dto.PublishRequest;
import com.peekmoment.backend.entity.Content;
import com.peekmoment.backend.entity.Media;
import com.peekmoment.backend.entity.Rating;
import com.peekmoment.backend.entity.Tag;
import com.peekmoment.backend.entity.User;
import com.peekmoment.backend.mapper.ContentMapper;
import com.peekmoment.backend.mapper.ContentTagMapper;
import com.peekmoment.backend.mapper.MediaMapper;
import com.peekmoment.backend.mapper.RatingMapper;
import com.peekmoment.backend.mapper.TagMapper;
import com.peekmoment.backend.mapper.UserMapper;
import com.peekmoment.backend.util.TimeUtil;

import lombok.RequiredArgsConstructor;

/**
 * 内容模块：发布、编辑、删除、时间线、详情。
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    private static final int MAX_IMAGES = 9;

    private final ContentMapper contentMapper;
    private final TagMapper tagMapper;
    private final MediaMapper mediaMapper;
    private final ContentTagMapper contentTagMapper;
    private final UserMapper userMapper;
    private final RatingMapper ratingMapper;

    @Transactional
    public ContentVO publish(Long userId, PublishRequest req) {
        validate(req);
        Content content = new Content();
        content.setUserId(userId);
        content.setTitle(blankToNull(req.title()));
        content.setBody(blankToNull(req.body()));
        content.setStatus(Content.STATUS_PUBLISHED);
        content.setRatingCount(0);
        content.setRatingAvg(0.0);
        content.setCommentCount(0);
        content.setDeleted(0);
        contentMapper.insert(content);

        saveTags(content.getId(), req.tags());
        saveMedia(content.getId(), req.media());
        return detail(content.getId(), userId);
    }

    @Transactional
    public ContentVO update(Long userId, Long contentId, PublishRequest req) {
        validate(req);
        Content content = requireOwned(userId, contentId);
        content.setTitle(blankToNull(req.title()));
        content.setBody(blankToNull(req.body()));
        contentMapper.updateById(content);

        contentTagMapper.deleteByContentId(contentId);
        saveTags(contentId, req.tags());
        mediaMapper.delete(new LambdaQueryWrapper<Media>()
                .eq(Media::getContentId, contentId));
        saveMedia(contentId, req.media());
        return detail(contentId, userId);
    }

    @Transactional
    public void delete(Long userId, Long contentId) {
        requireOwned(userId, contentId);
        contentMapper.deleteById(contentId);
    }

    public PageVO<ContentVO> timeline(long page, long size, Long viewerId) {
        Page<Content> result = contentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getStatus, Content.STATUS_PUBLISHED)
                        .orderByDesc(Content::getCreatedAt));
        List<ContentVO> vos = buildVOs(result.getRecords(), viewerId);
        return new PageVO<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    public ContentVO detail(Long contentId, Long viewerId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        boolean isAuthor = viewerId != null && viewerId.equals(content.getUserId());
        if (content.getStatus() != Content.STATUS_PUBLISHED && !isAuthor) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        return buildVOs(List.of(content), viewerId).get(0);
    }

    /** 检索：关键词（标题/正文）、精确标签、日期，可组合 */
    public PageVO<ContentVO> search(String keyword, String tag, String date,
            long page, long size, Long viewerId) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<Content>()
                .eq(Content::getStatus, Content.STATUS_PUBLISHED);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Content::getTitle, keyword)
                    .or().like(Content::getBody, keyword));
        }
        if (tag != null && !tag.isBlank()) {
            Tag t = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, tag.trim()));
            if (t == null) {
                return new PageVO<>(List.of(), 0, page, size);
            }
            List<Long> ids = contentTagMapper.selectByTagId(t.getId());
            if (ids.isEmpty()) {
                return new PageVO<>(List.of(), 0, page, size);
            }
            wrapper.in(Content::getId, ids);
        }
        if (date != null && !date.isBlank()) {
            try {
                LocalDate d = LocalDate.parse(date);
                wrapper.between(Content::getCreatedAt,
                        d.atStartOfDay(), d.atTime(LocalTime.MAX));
            } catch (DateTimeParseException e) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "日期格式应为 yyyy-MM-dd");
            }
        }
        wrapper.orderByDesc(Content::getCreatedAt);
        Page<Content> result = contentMapper.selectPage(new Page<>(page, size), wrapper);
        List<ContentVO> vos = buildVOs(result.getRecords(), viewerId);
        return new PageVO<>(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private Content requireOwned(Long userId, Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "动态不存在");
        }
        if (!content.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能操作自己的动态");
        }
        return content;
    }

    private void validate(PublishRequest req) {
        boolean hasText = (req.title() != null && !req.title().isBlank())
                || (req.body() != null && !req.body().isBlank());
        boolean hasMedia = req.media() != null && !req.media().isEmpty();
        if (!hasText && !hasMedia) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "标题、正文、附件至少填写一项");
        }
        if (!hasMedia) {
            return;
        }
        long images = req.media().stream().filter(m -> m.kind() == Media.KIND_IMAGE).count();
        long videos = req.media().stream().filter(m -> m.kind() == Media.KIND_VIDEO).count();
        if (images > MAX_IMAGES) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片最多 " + MAX_IMAGES + " 张");
        }
        if (videos > 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "视频最多 1 段");
        }
        if (videos > 0 && images > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "视频与图片需分别发布");
        }
    }

    private void saveTags(Long contentId, List<String> tags) {
        if (tags == null) {
            return;
        }
        List<String> distinct = tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .limit(5)
                .toList();
        for (String name : distinct) {
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, name));
            if (tag == null) {
                tag = new Tag();
                tag.setName(name);
                tagMapper.insert(tag);
            }
            contentTagMapper.insert(contentId, tag.getId());
        }
    }

    private void saveMedia(Long contentId, List<PublishRequest.MediaItem> media) {
        if (media == null) {
            return;
        }
        int order = 0;
        for (PublishRequest.MediaItem item : media) {
            Media entity = new Media();
            entity.setContentId(contentId);
            entity.setKind(item.kind());
            entity.setUrl(item.src());
            entity.setSortOrder(order++);
            mediaMapper.insert(entity);
        }
    }

    /** 批量组装视图：作者、媒体、标签、当前用户评分 */
    private List<ContentVO> buildVOs(List<Content> contents, Long viewerId) {
        if (contents.isEmpty()) {
            return List.of();
        }
        List<Long> contentIds = contents.stream().map(Content::getId).toList();
        List<Long> userIds = contents.stream().map(Content::getUserId).distinct().toList();

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, List<Media>> mediaMap = mediaMapper.selectList(
                new LambdaQueryWrapper<Media>().in(Media::getContentId, contentIds)
                        .orderByAsc(Media::getSortOrder)).stream()
                .collect(Collectors.groupingBy(Media::getContentId));
        Map<Long, List<String>> tagMap = contentTagMapper
                .selectTagsByContentIds(contentIds).stream()
                .collect(Collectors.groupingBy(ContentTagRow::getContentId,
                        Collectors.mapping(ContentTagRow::getName, Collectors.toList())));
        Map<Long, Integer> myScoreMap = viewerId == null ? Map.of()
                : ratingMapper.selectList(new LambdaQueryWrapper<Rating>()
                        .in(Rating::getContentId, contentIds)
                        .eq(Rating::getUserId, viewerId)).stream()
                        .collect(Collectors.toMap(Rating::getContentId, Rating::getScore));

        return contents.stream().map(c -> {
            User author = userMap.get(c.getUserId());
            List<ContentVO.MediaVO> media = mediaMap
                    .getOrDefault(c.getId(), List.of()).stream()
                    .map(m -> new ContentVO.MediaVO(m.getKind(), m.getUrl()))
                    .toList();
            Integer myScore = myScoreMap.get(c.getId());
            return new ContentVO(c.getId(), c.getUserId(),
                    author == null ? "未知用户" : author.getNickname(),
                    author == null ? null : author.getAvatarUrl(),
                    c.getTitle(), c.getBody(),
                    tagMap.getOrDefault(c.getId(), List.of()), media,
                    TimeUtil.toMillis(c.getCreatedAt()),
                    c.getRatingAvg(), c.getRatingCount(), c.getCommentCount(),
                    c.getStatus(), myScore);
        }).toList();
    }

    private String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
