package com.peekmoment.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.dto.LoginRequest;
import com.peekmoment.backend.dto.LoginResponse;
import com.peekmoment.backend.dto.RegisterRequest;
import com.peekmoment.backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 小程序端登录注册，公开接口。
 */
@RestController
@RequestMapping("/api/mp/auth")
@RequiredArgsConstructor
public class MpAuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }
}
