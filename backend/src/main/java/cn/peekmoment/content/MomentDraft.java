package cn.peekmoment.content;

import cn.peekmoment.common.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record MomentDraft(String title, String body, List<String> tags, List<Attachment> media) {
    public record Attachment(String kind, String src) {}
    private static final Set<String> FIELDS = Set.of("title", "body", "tags", "media");

    static MomentDraft from(JsonNode value) {
        if (value == null || !value.isObject()) throw invalid("请提交 JSON 对象");
        value.fieldNames().forEachRemaining(name -> {
            if (!FIELDS.contains(name)) throw invalid("不支持的发布字段：" + name);
        });
        String title = text(value, "title", 40);
        String body = text(value, "body", 1000);
        Set<String> tags = new LinkedHashSet<>();
        for (JsonNode tag : array(value, "tags", 5)) {
            if (!tag.isTextual()) throw invalid("标签需为文本");
            String name = tag.textValue().strip();
            if (name.isEmpty() || name.length() > 12) throw invalid("标签长度需为 1 至 12 字");
            if (!StandardCharsets.UTF_8.newEncoder().canEncode(name)) throw invalid("标签含有无效字符");
            tags.add(name);
        }
        List<Attachment> media = new ArrayList<>();
        for (JsonNode item : array(value, "media", 9)) {
            if (!item.isObject()) throw invalid("附件需包含 kind 和 src");
            String kind = text(item, "kind", 8);
            String src = text(item, "src", 2048);
            if (!kind.equals("image") && !kind.equals("video")) throw invalid("不支持的附件类型");
            if (!validSource(src)) throw invalid("请使用已上传的媒体地址");
            media.add(new Attachment(kind, src));
        }
        if (media.size() > 1 && media.stream().anyMatch(m -> m.kind().equals("video")))
            throw invalid("每条动态可选最多 9 张图片或 1 段视频");
        if (title.isEmpty() && body.isEmpty() && media.isEmpty()) throw invalid("请填写标题、正文或添加附件");
        return new MomentDraft(title, body, List.copyOf(tags), List.copyOf(media));
    }

    private static String text(JsonNode value, String field, int max) {
        JsonNode node = value.get(field);
        if (node == null) return "";
        if (!node.isTextual()) throw invalid(field + " 需为文本");
        String result = node.textValue().strip();
        if (result.length() > max) throw invalid(field + " 最多 " + max + " 字");
        if (!StandardCharsets.UTF_8.newEncoder().canEncode(result)) throw invalid(field + " 含有无效字符");
        return result;
    }

    private static Iterable<JsonNode> array(JsonNode value, String field, int max) {
        JsonNode node = value.get(field);
        if (node == null) return List.of();
        if (!node.isArray() || node.size() > max) throw invalid(field + " 需为数组，最多 " + max + " 项");
        return node;
    }

    private static boolean validSource(String src) {
        try {
            URI uri = new URI(src);
            if (uri.isAbsolute())
                return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null;
            String path = uri.getPath();
            return src.startsWith("/uploads/") && uri.getAuthority() == null && path != null && path.startsWith("/uploads/")
                && path.length() > "/uploads/".length() && !path.contains("\\")
                && Arrays.stream(path.split("/")).noneMatch(p -> p.equals(".") || p.equals(".."));
        } catch (URISyntaxException invalidUri) {
            return false;
        }
    }

    private static ApiException invalid(String message) { return new ApiException(400, message); }
}
