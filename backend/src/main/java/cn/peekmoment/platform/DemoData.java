package cn.peekmoment.platform;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test", "demo"})
@DependsOnDatabaseInitialization
class DemoData {
    private final JdbcTemplate jdbc;

    DemoData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    void seed() {
        jdbc.update("""
                INSERT INTO users (id, nickname, created_at) VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE id = id
                """, DevPublisher.USER_ID, "小满", System.currentTimeMillis());
    }
}
