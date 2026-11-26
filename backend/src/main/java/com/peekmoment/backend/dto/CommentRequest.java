package com.peekmoment.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 评论请求，parentId 为空是一级评论。
 */
public record CommentRequest(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 200, message = "评论最多 200 字") String body,
        Long parentId) {
}
