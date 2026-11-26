CREATE TABLE IF NOT EXISTS moments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(40) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    created_at BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'published',
    FOREIGN KEY (user_id) REFERENCES users(id),
    CHECK (status IN ('published', 'pending', 'hidden')),
    INDEX idx_feed (status, created_at, id),
    INDEX idx_admin_feed (created_at, id)
);

CREATE TABLE IF NOT EXISTS tags (
    tag_key VARBINARY(48) PRIMARY KEY,
    name VARCHAR(12) NOT NULL
);

CREATE TABLE IF NOT EXISTS moment_tags (
    moment_id BIGINT NOT NULL,
    tag_key VARBINARY(48) NOT NULL,
    sort_order INT NOT NULL CHECK (sort_order BETWEEN 0 AND 4),
    PRIMARY KEY (moment_id, tag_key),
    UNIQUE (moment_id, sort_order),
    FOREIGN KEY (moment_id) REFERENCES moments(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key)
);

CREATE TABLE IF NOT EXISTS moment_media (
    moment_id BIGINT NOT NULL,
    sort_order INT NOT NULL CHECK (sort_order BETWEEN 0 AND 8),
    kind VARCHAR(8) NOT NULL CHECK (kind IN ('image', 'video')),
    src VARCHAR(2048) NOT NULL,
    PRIMARY KEY (moment_id, sort_order),
    FOREIGN KEY (moment_id) REFERENCES moments(id) ON DELETE CASCADE
);
