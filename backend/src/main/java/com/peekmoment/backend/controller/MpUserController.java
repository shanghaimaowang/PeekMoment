package com.peekmoment.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.dto.PasswordRequest;
import com.peekmoment.backend.dto.UserUpdateRequest;
import com.peekmoment.backend.dto.UserVO;
import com.peekmoment.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 小程序端个人资料，全部需登录。
 */
@RestController
@RequestMapping("/api/mp/users")
@RequiredArgsConstructor
public class MpUserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(userService.me(UserContext.currentUserId()));
    }

    @PutMapping("/me")
    public Result<UserVO> update(@Valid @RequestBody UserUpdateRequest req) {
        return Result.ok(userService.update(UserContext.currentUserId(), req));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordRequest req) {
        userService.changePassword(UserContext.currentUserId(), req);
        return Result.ok();
    }
}
