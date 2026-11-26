package com.peekmoment.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * 修改资料请求。
 */
public record UserUpdateRequest(
        @Size(max = 32, message = "昵称最多 32 字") String nickname,
        @Size(max = 255, message = "头像地址过长") String avatarUrl) {
}
