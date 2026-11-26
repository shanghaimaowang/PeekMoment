package com.peekmoment.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 首次启动初始化演示管理员 admin / 123456（BCrypt 加密存储）。
 * 数据库未初始化时只打提示，不阻断启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin", Long.class);
            if (count != null && count == 0) {
                jdbcTemplate.update(
                        "INSERT INTO admin (username, password_hash, nickname) VALUES (?, ?, ?)",
                        "admin", BCrypt.hashpw("123456"), "管理员");
                log.info("已创建演示管理员 admin / 123456");
            }
        } catch (Exception e) {
            log.warn("数据库尚未初始化，请先执行 src/main/resources/db/schema.sql");
        }
    }
}
