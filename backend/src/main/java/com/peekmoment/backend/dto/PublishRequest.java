package com.peekmoment.backend.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 发布/编辑请求：标题、正文、附件至少一项。
 */
public record PublishRequest(
        @Size(max = 40, message = "标题最多 40 字") String title,
        @Size(max = 1000, message = "正文最多 1000 字") String body,
        @Size(max = 5, message = "标签最多 5 个") List<@Size(max = 12, message = "标签最多 12 字") String> tags,
        List<MediaItem> media) {

    public record MediaItem(
            @Min(value = 1, message = "媒体类型错误") @Max(value = 2, message = "媒体类型错误") Integer kind,
            @NotBlank(message = "媒体地址不能为空") @Size(max = 255) String src) {
    }
}
