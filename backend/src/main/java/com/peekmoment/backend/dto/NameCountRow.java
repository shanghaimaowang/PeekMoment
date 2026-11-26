package com.peekmoment.backend.dto;

import lombok.Data;

/**
 * 名称计数行：标签分布等。
 */
@Data
public class NameCountRow {

    private String name;

    private long cnt;
}
