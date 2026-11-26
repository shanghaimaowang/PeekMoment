package com.peekmoment.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.auth.UserContext;
import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.dto.CommentRequest;
import com.peekmoment.backend.dto.CommentVO;
import com.peekmoment.backend.dto.RatingRequest;
import com.peekmoment.backend.service.InteractionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 互动接口：评论列表游客可看，评分与发评论需登录。
 */
@RestController
@RequestMapping("/api/mp/contents/{contentId}")
@RequiredArgsConstructor
public class MpInteractionController {

    private final InteractionService interactionService;

    @PostMapping("/ratings")
    public Result<Integer> rate(@PathVariable Long contentId,
            @Valid @RequestBody RatingRequest req) {
        return Result.ok(interactionService.rate(UserContext.currentUserId(), contentId, req));
    }

    @GetMapping("/comments")
    public Result<List<CommentVO>> comments(@PathVariable Long contentId) {
        return Result.ok(interactionService.comments(contentId));
    }

    @PostMapping("/comments")
    public Result<CommentVO> addComment(@PathVariable Long contentId,
            @Valid @RequestBody CommentRequest req) {
        return Result.ok(interactionService.addComment(
                UserContext.currentUserId(), contentId, req));
    }
}
