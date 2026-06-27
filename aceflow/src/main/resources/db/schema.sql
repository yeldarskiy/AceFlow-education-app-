-- AceFlow Database Schema
-- Encoding: UTF-8
-- DBMS: PostgreSQL

SET client_encoding = 'UTF8';

-- ── Drop tables in reverse dependency order ──
DROP TABLE IF EXISTS user_achievements CASCADE;
DROP TABLE IF EXISTS test_results     CASCADE;
DROP TABLE IF EXISTS flashcards       CASCADE;
DROP TABLE IF EXISTS questions        CASCADE;
DROP TABLE IF EXISTS tests            CASCADE;
DROP TABLE IF EXISTS goals            CASCADE;
DROP TABLE IF EXISTS study_plans      CASCADE;
DROP TABLE IF EXISTS documents        CASCADE;
DROP TABLE IF EXISTS achievements     CASCADE;
DROP TABLE IF EXISTS users            CASCADE;

-- ── 1. users ──────────────────────────────────────────────────────────────
CREATE TABLE users (
    user_id       SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    xp_points     INT          NOT NULL DEFAULT 0,
    streak_days   INT          NOT NULL DEFAULT 0,
    role          VARCHAR(20)  NOT NULL DEFAULT 'STUDENT'
                      CHECK (role IN ('STUDENT', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── 2. documents ──────────────────────────────────────────────────────────
CREATE TABLE documents (
    doc_id        SERIAL PRIMARY KEY,
    user_id       INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title         VARCHAR(200) NOT NULL,
    file_type     VARCHAR(20)  NOT NULL CHECK (file_type IN ('PDF', 'DOCX', 'TXT')),
    file_url      VARCHAR(500) NOT NULL,
    file_size     BIGINT       NOT NULL DEFAULT 0,
    language      VARCHAR(10)  NOT NULL DEFAULT 'en',
    uploaded_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── 3. study_plans ────────────────────────────────────────────────────────
CREATE TABLE study_plans (
    plan_id       SERIAL PRIMARY KEY,
    user_id       INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    exam_name     VARCHAR(100) NOT NULL,
    exam_type     VARCHAR(50),
    exam_date     DATE         NOT NULL,
    start_date    DATE         NOT NULL DEFAULT CURRENT_DATE,
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'COMPLETED', 'PAUSED'))
);

-- ── 4. goals ──────────────────────────────────────────────────────────────
CREATE TABLE goals (
    goal_id       SERIAL PRIMARY KEY,
    user_id       INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title         VARCHAR(200) NOT NULL,
    deadline      DATE,
    is_completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    xp_reward     INT          NOT NULL DEFAULT 100,
    priority      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM'
                      CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

-- ── 5. tests ──────────────────────────────────────────────────────────────
CREATE TABLE tests (
    test_id       SERIAL PRIMARY KEY,
    doc_id        INT          REFERENCES documents(doc_id) ON DELETE SET NULL,
    title         VARCHAR(200) NOT NULL,
    test_type     VARCHAR(20)  NOT NULL DEFAULT 'MULTIPLE_CHOICE'
                      CHECK (test_type IN ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'MIXED')),
    time_limit    INT          NOT NULL DEFAULT 30,
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM'
                      CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── 6. questions ──────────────────────────────────────────────────────────
CREATE TABLE questions (
    question_id   SERIAL PRIMARY KEY,
    test_id       INT          NOT NULL REFERENCES tests(test_id) ON DELETE CASCADE,
    question_text TEXT         NOT NULL,
    correct_answer TEXT        NOT NULL,
    option_a      VARCHAR(500),
    option_b      VARCHAR(500),
    option_c      VARCHAR(500),
    option_d      VARCHAR(500),
    question_type VARCHAR(20)  NOT NULL DEFAULT 'MULTIPLE_CHOICE',
    difficulty    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    points        INT          NOT NULL DEFAULT 10,
    explanation   TEXT
);

-- ── 7. test_results ───────────────────────────────────────────────────────
CREATE TABLE test_results (
    result_id     SERIAL PRIMARY KEY,
    user_id       INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    test_id       INT          NOT NULL REFERENCES tests(test_id) ON DELETE CASCADE,
    score         DECIMAL(5,2) NOT NULL,
    duration      INT          NOT NULL DEFAULT 0,
    completed_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── 8. flashcards ─────────────────────────────────────────────────────────
CREATE TABLE flashcards (
    card_id         SERIAL PRIMARY KEY,
    doc_id          INT          REFERENCES documents(doc_id) ON DELETE SET NULL,
    user_id         INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    front_text      TEXT         NOT NULL,
    back_text       TEXT         NOT NULL,
    difficulty      VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    times_reviewed  INT          NOT NULL DEFAULT 0,
    next_review     DATE,
    is_mastered     BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ── 9. achievements ───────────────────────────────────────────────────────
CREATE TABLE achievements (
    achievement_id   SERIAL PRIMARY KEY,
    title            VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    xp_reward        INT          NOT NULL DEFAULT 100,
    category         VARCHAR(50),
    condition_type   VARCHAR(50)  NOT NULL,
    condition_value  INT          NOT NULL DEFAULT 1
);

-- ── 10. user_achievements ─────────────────────────────────────────────────
CREATE TABLE user_achievements (
    id               SERIAL PRIMARY KEY,
    user_id          INT          NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    achievement_id   INT          NOT NULL REFERENCES achievements(achievement_id) ON DELETE CASCADE,
    earned_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    is_seen          BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, achievement_id)
);

-- ── Indexes for performance ────────────────────────────────────────────────
CREATE INDEX idx_documents_user_id     ON documents(user_id);
CREATE INDEX idx_study_plans_user_id   ON study_plans(user_id);
CREATE INDEX idx_goals_user_id         ON goals(user_id);
CREATE INDEX idx_test_results_user_id  ON test_results(user_id);
CREATE INDEX idx_test_results_test_id  ON test_results(test_id);
CREATE INDEX idx_flashcards_user_id    ON flashcards(user_id);
CREATE INDEX idx_flashcards_doc_id     ON flashcards(doc_id);
CREATE INDEX idx_questions_test_id     ON questions(test_id);
CREATE INDEX idx_user_achievements_uid ON user_achievements(user_id);
