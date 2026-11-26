package com.peekmoment.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.common.Result;

/**
 * 健康检查，同时验证统一响应体格式。
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Result<String> health() {
        return Result.ok("PeekMoment backend 0.1.0 is running");
    }
}
