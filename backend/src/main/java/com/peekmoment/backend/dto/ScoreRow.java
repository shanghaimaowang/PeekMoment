package com.peekmoment.backend.dto;

import lombok.Data;

/**
 * 评分分布行。
 */
@Data
public class ScoreRow {

    private Integer score;

    private long cnt;
}
