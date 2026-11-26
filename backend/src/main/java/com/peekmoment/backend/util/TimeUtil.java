package com.peekmoment.backend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 时间转换：数据库 DATETIME → 前端毫秒时间戳。
 */
public class TimeUtil {

    public static Long toMillis(LocalDateTime time) {
        return time == null ? null
                : time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
