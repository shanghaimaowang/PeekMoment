package cn.peekmoment.common;

import java.util.List;
import org.springframework.util.MultiValueMap;

public record Paging(int page, int pageSize) {
    public static Paging from(MultiValueMap<String, String> params) {
        return new Paging(read(params, "page", 1, Integer.MAX_VALUE), read(params, "pageSize", 10, 100));
    }

    public long offset() {
        return (long) (page - 1) * pageSize;
    }

    private static int read(MultiValueMap<String, String> params, String key, int fallback, int max) {
        List<String> values = params.get(key);
        if (values == null) return fallback;
        if (values.size() == 1 && values.get(0) != null && values.get(0).matches("[1-9][0-9]{0,9}")) {
            long value = Long.parseLong(values.get(0));
            if (value <= max) return (int) value;
        }
        throw new ApiException(400, key + " 必须是 1 到 " + max + " 之间的整数");
    }
}
