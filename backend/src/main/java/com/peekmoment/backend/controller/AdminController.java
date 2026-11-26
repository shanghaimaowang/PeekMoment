package com.peekmoment.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peekmoment.backend.common.Result;
import com.peekmoment.backend.dto.ContentVO;
import com.peekmoment.backend.dto.NameCountRow;
import com.peekmoment.backend.dto.PageVO;
import com.peekmoment.backend.dto.ScoreRow;
import com.peekmoment.backend.dto.StatusRequest;
import com.peekmoment.backend.dto.SummaryVO;
import com.peekmoment.backend.dto.TrendRow;
import com.peekmoment.backend.dto.UserVO;
import com.peekmoment.backend.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 管理端接口，/api/admin/** 拦截器要求 admin 角色。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public Result<PageVO<UserVO>> users(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.users(page, Math.min(size, 50), keyword));
    }

    @GetMapping("/users/{id}")
    public Result<UserVO> userDetail(@PathVariable Long id) {
        return Result.ok(adminService.userDetail(id));
    }

    @PutMapping("/users/{id}/status")
    public Result<UserVO> updateUserStatus(@PathVariable Long id,
            @Valid @RequestBody StatusRequest req) {
        if (req.status() != 0 && req.status() != 1) {
            return Result.fail(400, "用户状态只能为 0 或 1");
        }
        return Result.ok(adminService.updateUserStatus(id, req.status()));
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok();
    }

    @GetMapping("/contents")
    public Result<PageVO<ContentVO>> contents(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(adminService.contents(page, Math.min(size, 50), status));
    }

    @PutMapping("/contents/{id}/status")
    public Result<ContentVO> updateContentStatus(@PathVariable Long id,
            @Valid @RequestBody StatusRequest req) {
        if (req.status() != 1 && req.status() != 3) {
            return Result.fail(400, "内容状态只能为 1（发布）或 3（下架）");
        }
        return Result.ok(adminService.updateContentStatus(id, req.status()));
    }

    @DeleteMapping("/contents/{id}")
    public Result<Void> deleteContent(@PathVariable Long id) {
        adminService.deleteContent(id);
        return Result.ok();
    }

    @GetMapping("/stats/summary")
    public Result<SummaryVO> summary() {
        return Result.ok(adminService.summary());
    }

    @GetMapping("/stats/trend")
    public Result<List<TrendRow>> trend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(adminService.trend(Math.min(days, 30)));
    }

    @GetMapping("/stats/distribution")
    public Result<DistributionVO> distribution() {
        return Result.ok(new DistributionVO(
                adminService.tagDistribution(), adminService.scoreDistribution()));
    }

    public record DistributionVO(List<NameCountRow> tags, List<ScoreRow> scores) {
    }
}
