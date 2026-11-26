package com.peekmoment.backend.dto;

import java.util.List;

/**
 * 评论视图，二级回复挂在 replies。
 */
public record CommentVO(Long id, Long userId, String author, String body, long at,
        List<CommentVO> replies) {
}
