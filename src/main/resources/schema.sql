-- Таблица Post
CREATE TABLE IF NOT EXISTS post(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT,
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    image     BYTEA
    );

-- Таблица Comment
CREATE TABLE IF NOT EXISTS comment(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text TEXT,
    post_id BIGINT NOT NULL,

    CONSTRAINT fk_comment_post
    FOREIGN KEY (post_id)
    REFERENCES post (id)
    ON DELETE CASCADE
    );

-- Таблица Tag
CREATE TABLE IF NOT EXISTS tag(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
    );

-- ManyToMany Post-Tag
CREATE TABLE IF NOT EXISTS post_tag(
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    CONSTRAINT fk_posttag_post
    FOREIGN KEY (post_id)
    REFERENCES post (id)
    ON DELETE CASCADE,
    CONSTRAINT fk_posttag_tag
    FOREIGN KEY (tag_id)
    REFERENCES tag (id)
    ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
    );