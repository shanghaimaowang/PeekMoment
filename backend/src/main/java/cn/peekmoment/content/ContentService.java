package cn.peekmoment.content;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.peekmoment.content.MomentDraft.Attachment;

@Service
public class ContentService {
    public record View(String id, String userId, String author, String title, String body, List<String> tags,
                       List<Attachment> media, long createdAt, String status, Map<String, Integer> ratings,
                       List<Object> comments) {}
    public record Page(List<View> items, int page, int pageSize, long total) {}

    private final MomentMapper mapper;
    public ContentService(MomentMapper mapper) { this.mapper = mapper; }

    @Transactional
    public View publish(String userId, MomentDraft draft) {
        MomentEntity entity = new MomentEntity();
        entity.setUserId(userId);
        entity.setTitle(draft.title());
        entity.setBody(draft.body());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setStatus("published");
        mapper.insert(entity);
        long id = entity.getId();

        // Acquire shared tag keys in a consistent order across concurrent publishers.
        for (String name : draft.tags().stream().sorted().toList())
            mapper.ensureTag(name.getBytes(StandardCharsets.UTF_8), name);
        for (int i = 0; i < draft.tags().size(); i++)
            mapper.attachTag(id, draft.tags().get(i).getBytes(StandardCharsets.UTF_8), i);
        for (int i = 0; i < draft.media().size(); i++) {
            Attachment attachment = draft.media().get(i);
            mapper.attachMedia(id, attachment.kind(), attachment.src(), i);
        }
        return view(mapper.moment(id), draft.tags(), draft.media());
    }

    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public Page page(int page, int pageSize, boolean admin, String keyword) {
        long total = mapper.countPage(admin, keyword);
        long offset = ((long) page - 1) * pageSize;
        if (offset >= total) return new Page(List.of(), page, pageSize, total);
        List<MomentMapper.Row> rows = mapper.pageRows(admin, keyword, pageSize, offset);
        if (rows.isEmpty()) return new Page(List.of(), page, pageSize, total);
        List<Long> ids = rows.stream().map(MomentMapper.Row::id).toList();
        Map<Long, List<String>> tags = new HashMap<>();
        for (MomentMapper.Tag tag : mapper.tags(ids))
            tags.computeIfAbsent(tag.momentId(), key -> new ArrayList<>()).add(tag.name());
        Map<Long, List<Attachment>> media = new HashMap<>();
        for (MomentMapper.Media item : mapper.media(ids))
            media.computeIfAbsent(item.momentId(), key -> new ArrayList<>()).add(new Attachment(item.kind(), item.src()));
        List<View> items = rows.stream().map(r -> view(r, tags.getOrDefault(r.id(), List.of()),
                media.getOrDefault(r.id(), List.of()))).toList();
        return new Page(items, page, pageSize, total);
    }

    private static View view(MomentMapper.Row row, List<String> tags, List<Attachment> media) {
        return new View(String.valueOf(row.id()), row.userId(), row.author(), row.title(), row.body(), tags, media,
                row.createdAt(), row.status(), Map.of(), List.of());
    }
}
