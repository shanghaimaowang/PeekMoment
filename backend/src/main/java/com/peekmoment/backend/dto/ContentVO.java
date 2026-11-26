package com.peekmoment.backend.dto;

import java.util.List;

/**
 * 动态视图，字段对齐小程序本地数据结构。
 */
public record ContentVO(
        Long id,
        Long userId,
        String author,
        String avatarUrl,
        String title,
        String body,
        List<String> tags,
        List<MediaVO> media,
        long createdAt,
        Double ratingAvg,
        Integer ratingCount,
        Integer commentCount,
        Integer status,
        Integer myScore) {

    public record MediaVO(Integer kind, String src) {
    }
}
