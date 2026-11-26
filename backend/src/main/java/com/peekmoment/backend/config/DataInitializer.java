package com.peekmoment.backend.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 首次启动初始化：演示管理员 admin / 123456；动态内容不足 5 条时灌入演示数据。
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
            seedAdmin();
            seedDemoContents();
        } catch (Exception e) {
            log.warn("数据库尚未初始化，请先执行 src/main/resources/db/schema.sql");
        }
    }

    private void seedAdmin() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admin", Long.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO admin (username, password_hash, nickname) VALUES (?, ?, ?)",
                    "admin", BCrypt.hashpw("123456"), "管理员");
            log.info("已创建演示管理员 admin / 123456");
        }
    }

    /** 灌入演示用户、动态、标签、评分与评论，保证时间线和统计有内容可看 */
    private void seedDemoContents() {
        Long contentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM content", Long.class);
        if (contentCount != null && contentCount >= 5) {
            return;
        }

        Long xiaomanId = ensureUser("xiaoman", "小满");
        Long ashuId = ensureUser("ashu", "阿树");
        Long juziId = ensureUser("juzi", "橘子");

        insertContent(ashuId, "三食堂二楼新开的麻辣香锅",
                "亲测不踩雷！午餐肉给得很大方，微辣刚刚好，人均 15 块吃到扶墙。",
                1, List.of("校园", "美食"),
                Map.of(xiaomanId, 5, juziId, 4),
                List.of(juziId, "求具体在哪个窗口", xiaomanId, "下周冲"));
        insertContent(juziId, "图书馆四楼靠窗的位置真的好安静",
                "期末周抢救圣地，插座充足，下午阳光晒进来整个人都清醒了。提醒：周五晚上闭馆早。",
                2, List.of("校园", "学习"),
                Map.of(xiaomanId, 4),
                List.of(ashuId, "收藏了，明天就去"));
        insertContent(xiaomanId, "傍晚的操场天空绝了",
                "跑完步抬头看到粉紫色的晚霞，随手一拍都是壁纸，这就是大学生活的浪漫吧。",
                3, List.of("校园"),
                Map.of(ashuId, 5),
                List.of(juziId, "想看原图！"));
        insertContent(ashuId, "捡到一张校园卡 尾号 0883",
                "在篮球场边捡到的，失主看到请评论联系我，或者直接送到 3 号宿舍楼值班室。",
                4, List.of("失物招领"),
                Map.of(),
                List.of(juziId, "我同学的！已经让他联系你了"));
        insertContent(juziId, "周末去了趟良渚遗址公园",
                "五千年前的人类智慧，草坪和鹿都很治愈，适合骑车慢慢逛，门票预约别忘了。",
                5, List.of("旅行", "周末"),
                Map.of(xiaomanId, 5, ashuId, 4),
                List.of(xiaomanId, "下周组队！"));
        insertContent(xiaomanId, "宿舍楼下的猫又胖了",
                "橘子已晋升为宿舍区一哥，投喂请注意适量，胖是可爱但有健康风险。",
                6, List.of("校园", "毛孩子"),
                Map.of(juziId, 5),
                List.of(ashuId, "橘子本橘确实圆润"));

        log.info("已灌入演示动态数据（6 条，含标签/评分/评论）");
    }

    private Long ensureUser(String username, String nickname) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id FROM user WHERE username = ?", username);
        if (!rows.isEmpty()) {
            return ((Number) rows.get(0).get("id")).longValue();
        }
        jdbcTemplate.update(
                "INSERT INTO user (username, password_hash, nickname, status, deleted) "
                        + "VALUES (?, ?, ?, 1, 0)",
                username, BCrypt.hashpw("123456"), nickname);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM user WHERE username = ?", Long.class, username);
    }

    private void insertContent(Long userId, String title, String body, int daysAgo,
            List<String> tags, Map<Long, Integer> ratings, List<Object> comments) {
        jdbcTemplate.update(
                "INSERT INTO content (user_id, title, body, status, rating_count, "
                        + "rating_avg, comment_count, deleted, created_at) "
                        + "VALUES (?, ?, ?, 1, ?, ?, ?, 0, DATE_SUB(NOW(), INTERVAL ? DAY))",
                userId, title, body, ratings.size(),
                ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0),
                comments.size() / 2, daysAgo);

        Long contentId = jdbcTemplate.queryForObject(
                "SELECT id FROM content ORDER BY id DESC LIMIT 1", Long.class);

        for (String tag : tags) {
            jdbcTemplate.update("INSERT IGNORE INTO tag (name) VALUES (?)", tag);
            Long tagId = jdbcTemplate.queryForObject(
                    "SELECT id FROM tag WHERE name = ?", Long.class, tag);
            jdbcTemplate.update(
                    "INSERT IGNORE INTO content_tag (content_id, tag_id) VALUES (?, ?)",
                    contentId, tagId);
        }
        ratings.forEach((uid, score) -> jdbcTemplate.update(
                "INSERT INTO rating (content_id, user_id, score) VALUES (?, ?, ?)",
                contentId, uid, score));
        for (int i = 0; i + 1 < comments.size(); i += 2) {
            jdbcTemplate.update(
                    "INSERT INTO comment (content_id, user_id, body, status, deleted) "
                            + "VALUES (?, ?, ?, 1, 0)",
                    contentId, comments.get(i), comments.get(i + 1));
        }
    }
}
