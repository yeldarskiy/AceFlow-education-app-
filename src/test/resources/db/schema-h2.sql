-- H2-compatible schema for integration tests (MySQL mode)
SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS test_results;
DROP TABLE IF EXISTS flashcards;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS tests;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS study_plans;
DROP TABLE IF EXISTS documents;
DROP TABLE IF EXISTS achievements;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    xp_points     INT          NOT NULL DEFAULT 0,
    streak_days   INT          NOT NULL DEFAULT 0,
    role          VARCHAR(20)  NOT NULL DEFAULT 'STUDENT',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE documents (
    doc_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    title         VARCHAR(200) NOT NULL,
    file_type     VARCHAR(20)  NOT NULL,
    file_url      VARCHAR(500) NOT NULL,
    file_size     BIGINT       NOT NULL DEFAULT 0,
    language      VARCHAR(10)  NOT NULL DEFAULT 'en',
    uploaded_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE study_plans (
    plan_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    exam_name     VARCHAR(100) NOT NULL,
    exam_type     VARCHAR(50),
    exam_date     DATE         NOT NULL,
    start_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE goals (
    goal_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    title         VARCHAR(200) NOT NULL,
    deadline      DATE,
    is_completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    xp_reward     INT          NOT NULL DEFAULT 100,
    priority      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE tests (
    test_id       INT AUTO_INCREMENT PRIMARY KEY,
    doc_id        INT,
    title         VARCHAR(200) NOT NULL,
    test_type     VARCHAR(20)  NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    time_limit    INT          NOT NULL DEFAULT 30,
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doc_id) REFERENCES documents(doc_id) ON DELETE SET NULL
);

CREATE TABLE questions (
    question_id   INT AUTO_INCREMENT PRIMARY KEY,
    test_id       INT          NOT NULL,
    question_text TEXT         NOT NULL,
    correct_answer VARCHAR(10) NOT NULL,
    option_a      VARCHAR(500),
    option_b      VARCHAR(500),
    option_c      VARCHAR(500),
    option_d      VARCHAR(500),
    question_type VARCHAR(20)  NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    points        INT          NOT NULL DEFAULT 10,
    explanation   TEXT,
    FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE
);

CREATE TABLE test_results (
    result_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT          NOT NULL,
    test_id       INT          NOT NULL,
    score         DECIMAL(5,2) NOT NULL,
    duration      INT          NOT NULL DEFAULT 0,
    completed_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES tests(test_id) ON DELETE CASCADE
);

CREATE TABLE flashcards (
    card_id         INT AUTO_INCREMENT PRIMARY KEY,
    doc_id          INT,
    user_id         INT          NOT NULL,
    front_text      TEXT         NOT NULL,
    back_text       TEXT         NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    times_reviewed  INT          NOT NULL DEFAULT 0,
    next_review     DATE,
    is_mastered     BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (doc_id) REFERENCES documents(doc_id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE achievements (
    achievement_id   INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    xp_reward        INT          NOT NULL DEFAULT 100,
    category         VARCHAR(50),
    condition_type   VARCHAR(50)  NOT NULL,
    condition_value  INT          NOT NULL DEFAULT 1
);

CREATE TABLE user_achievements (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NOT NULL,
    achievement_id   INT          NOT NULL,
    earned_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_seen          BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, achievement_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_id) REFERENCES achievements(achievement_id) ON DELETE CASCADE
);

INSERT INTO users (name, email, password_hash, xp_points, streak_days, role) VALUES
    ('Test User', 'test@example.kz', '$2a$10$hash', 100, 1, 'STUDENT');

INSERT INTO achievements (title, description, xp_reward, category, condition_type, condition_value) VALUES
    ('First Win', 'Complete first test', 100, 'TESTS', 'TESTS_COUNT', 1),
    ('Streak', '7 day streak', 200, 'STREAK', 'STREAK_DAYS', 7);

INSERT INTO tests (title, test_type, time_limit, difficulty) VALUES
    ('Sample Test', 'MULTIPLE_CHOICE', 30, 'MEDIUM');

INSERT INTO questions (test_id, question_text, correct_answer, option_a, option_b) VALUES
    (1, 'What is 2+2?', 'A', '4', '5');

SET REFERENTIAL_INTEGRITY TRUE;
