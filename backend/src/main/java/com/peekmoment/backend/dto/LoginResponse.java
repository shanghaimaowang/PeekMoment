package com.peekmoment.backend.dto;

/**
 * 登录/注册响应：token 加用户信息。
 */
public record LoginResponse(String token, UserInfo user) {

    public record UserInfo(Long id, String username, String nickname, String avatarUrl) {
    }
}
