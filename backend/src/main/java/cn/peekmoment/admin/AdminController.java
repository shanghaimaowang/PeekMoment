package cn.peekmoment.admin;

import cn.peekmoment.common.ApiResponse;
import cn.peekmoment.common.Paging;
import cn.peekmoment.content.ContentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminSessions sessions;
    private final AdminUserMapper users;
    private final ContentService content;

    public AdminController(AdminUserMapper users, ContentService content,
                           @Value("${peek.admin-username:}") String username,
                           @Value("${peek.admin-password:}") String password) {
        this.sessions = new AdminSessions(username, password);
        this.users = users;
        this.content = content;
    }

    public record LoginInput(String username, String password) {}
    public record UserPage(List<AdminUser> items, int page, int pageSize, long total) {}

    @PostMapping("/login")
    public ApiResponse<AdminSessions.LoginResult> login(@RequestBody LoginInput body, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return ApiResponse.ok(sessions.login(body.username(), body.password()));
    }

    @GetMapping("/users")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ApiResponse<UserPage> users(@RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestParam MultiValueMap<String, String> params,
                                       HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        sessions.requireBearer(token);
        Paging paging = Paging.from(params);
        String keyword = keyword(params);
        QueryWrapper<AdminUser> query = new QueryWrapper<>();
        if (!keyword.isEmpty()) {
            query.apply("(LOCATE(LOWER({0}), LOWER(id)) > 0 OR LOCATE(LOWER({0}), LOWER(nickname)) > 0)", keyword);
        }
        long total = users.selectCount(query);
        query.orderByDesc("created_at").orderByAsc("id");
        // Paging accepts only bounded integers. MyBatis-Plus appends the clause after ORDER BY.
        query.last("LIMIT " + paging.pageSize() + " OFFSET " + paging.offset());
        return ApiResponse.ok(new UserPage(users.selectList(query), paging.page(), paging.pageSize(), total));
    }

    @GetMapping("/moments")
    public ApiResponse<ContentService.Page> moments(@RequestHeader(value = "Authorization", required = false) String token,
                                                     @RequestParam MultiValueMap<String, String> params,
                                                     HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        sessions.requireBearer(token);
        Paging paging = Paging.from(params);
        return ApiResponse.ok(content.page(paging.page(), paging.pageSize(), true, keyword(params)));
    }

    private static String keyword(MultiValueMap<String, String> params) {
        String value = params.getFirst("keyword");
        return value == null ? "" : value.strip();
    }
}
