package com.peekmoment.backend.dto;

import java.util.List;

/**
 * 分页响应。
 */
public record PageVO<T>(List<T> list, long total, long page, long size) {
}
