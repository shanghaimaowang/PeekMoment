package com.peekmoment.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 32, message = "用户名需 3-32 字符") String username,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码需 6-32 字符") String password,
        @NotBlank(message = "昵称不能为空")
        @Size(max = 32, message = "昵称最多 32 字符") String nickname) {
}
