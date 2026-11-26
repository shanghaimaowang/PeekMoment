package com.peekmoment.backend.dto;

/**
 * 用户视图。
 */
public record UserVO(Long id, String username, String nickname, String avatarUrl,
        Integer status, long createdAt) {
}
