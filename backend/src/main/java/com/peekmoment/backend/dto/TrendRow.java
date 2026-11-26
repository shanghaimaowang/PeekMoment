package com.peekmoment.backend.dto;

import lombok.Data;

/**
 * 趋势行：日期 + 数量。
 */
@Data
public class TrendRow {

    private String day;

    private long cnt;
}
