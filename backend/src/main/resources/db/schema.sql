-- ============================================================
-- PeekMoment 建表脚本（MySQL 8，utf8mb4）
-- 用法：mysql -uroot -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS peekmoment
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE peekmoment;

-- 用户表（小程序端）
CREATE TABLE user (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL COMMENT '登录名',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    nickname      VARCHAR(32)  NOT NULL COMMENT '昵称',
    avatar_url    VARCHAR(255) NULL COMMENT '头像地址',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    deleted       TINYINT      NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_username (username)
) COMMENT '用户';

-- 管理员表（管理后台）
CREATE TABLE admin (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(32)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_admin_username (username)
) COMMENT '管理员';

-- 动态内容表
CREATE TABLE content (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT UNSIGNED NOT NULL,
    title         VARCHAR(40)  NULL COMMENT '标题，选填',
    body          VARCHAR(1000) NULL COMMENT '正文',
    status        TINYINT      NOT NULL DEFAULT 1
                  COMMENT '1 已发布 2 待审核 3 已下架',
    rating_count  INT          NOT NULL DEFAULT 0 COMMENT '评分人数',
    rating_avg    DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT '平均分 0.00-5.00',
    comment_count INT          NOT NULL DEFAULT 0,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_content_user (user_id),
    KEY idx_content_created (created_at)
) COMMENT '动态';

-- 标签表
CREATE TABLE tag (
    id   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(12) NOT NULL,
    UNIQUE KEY uk_tag_name (name)
) COMMENT '标签';

-- 动态-标签关联表
CREATE TABLE content_tag (
    content_id BIGINT UNSIGNED NOT NULL,
    tag_id     BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (content_id, tag_id)
) COMMENT '动态标签关联';

-- 媒体附件表
CREATE TABLE media (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT UNSIGNED NOT NULL,
    kind       TINYINT      NOT NULL COMMENT '1 图片 2 视频',
    url        VARCHAR(255) NOT NULL COMMENT '可访问地址',
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_media_content (content_id)
) COMMENT '媒体附件';

-- 评分表（一人一评，重复评分覆盖）
CREATE TABLE rating (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT UNSIGNED NOT NULL,
    user_id    BIGINT UNSIGNED NOT NULL,
    score      TINYINT     NOT NULL COMMENT '1-5',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_rating_content_user (content_id, user_id)
) COMMENT '评分';

-- 评论表（parent_id 为空是一级评论，否则是二级回复）
CREATE TABLE comment (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT UNSIGNED  NOT NULL,
    user_id    BIGINT UNSIGNED  NOT NULL,
    parent_id  BIGINT UNSIGNED  NULL COMMENT '父评论 id',
    body       VARCHAR(200)     NOT NULL,
    status     TINYINT          NOT NULL DEFAULT 1 COMMENT '1 正常 2 待审核',
    deleted    TINYINT          NOT NULL DEFAULT 0,
    created_at DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_comment_content (content_id)
) COMMENT '评论';

-- 好友关系表
CREATE TABLE friend (
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NOT NULL COMMENT '发起方',
    friend_id  BIGINT UNSIGNED NOT NULL COMMENT '接收方',
    status     TINYINT         NOT NULL DEFAULT 0
               COMMENT '0 待确认 1 已通过 2 已拒绝',
    created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_friend_pair (user_id, friend_id)
) COMMENT '好友关系';

-- 管理端操作日志表
CREATE TABLE operation_log (
    id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    admin_id    BIGINT UNSIGNED NOT NULL,
    action      VARCHAR(32)   NOT NULL COMMENT '如 disable_user、remove_content',
    target_type VARCHAR(32)   NOT NULL COMMENT 'user / content / comment',
    target_id   BIGINT UNSIGNED NOT NULL,
    detail      VARCHAR(500)  NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_log_admin (admin_id)
) COMMENT '操作日志';
