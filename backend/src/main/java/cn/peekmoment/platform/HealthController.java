package cn.peekmoment.platform;

import cn.peekmoment.common.ApiResponse;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
    private final JdbcTemplate jdbc;

    HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    ApiResponse<Map<String, String>> health() {
        jdbc.queryForObject("SELECT 1", Integer.class);
        return ApiResponse.ok(Map.of("status", "ok"));
    }
}
