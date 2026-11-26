package com.peekmoment.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.service.UploadService;

import lombok.RequiredArgsConstructor;

/**
 * 媒体上传，需登录。
 */
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/api/mp/uploads")
    public Result<UploadVO> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(new UploadVO(uploadService.save(file)));
    }

    public record UploadVO(String url) {
    }
}
