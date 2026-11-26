package com.peekmoment.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.dto.ContentVO;
import com.peekmoment.backend.dto.PageVO;
import com.peekmoment.backend.dto.PublishRequest;
import com.peekmoment.backend.service.ContentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 内容接口：时间线与详情游客可看，写操作需登录。
 */
@RestController
@RequestMapping("/api/mp/contents")
@RequiredArgsConstructor
public class MpContentController {

    private final ContentService contentService;

    @GetMapping
    public Result<PageVO<ContentVO>> timeline(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Long viewerId = UserContext.currentUserId();
        return Result.ok(contentService.timeline(page, Math.min(size, 50), viewerId));
    }

    @GetMapping("/{id}")
    public Result<ContentVO> detail(@PathVariable Long id) {
        return Result.ok(contentService.detail(id, UserContext.currentUserId()));
    }

    @PostMapping
    public Result<ContentVO> publish(@Valid @RequestBody PublishRequest req) {
        return Result.ok(contentService.publish(UserContext.currentUserId(), req));
    }

    @PutMapping("/{id}")
    public Result<ContentVO> update(@PathVariable Long id,
            @Valid @RequestBody PublishRequest req) {
        return Result.ok(contentService.update(UserContext.currentUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.delete(UserContext.currentUserId(), id);
        return Result.ok();
    }
}
