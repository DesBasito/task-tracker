-- liquibase formatted sql

-- changeset abu:create-tasks-table
-- comment: Создание таблицы задач
CREATE TABLE IF NOT EXISTS tasks
(
    id          BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    title       VARCHAR(255)          NOT NULL,
    description TEXT,
    status      VARCHAR(50)           NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP             NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_task_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'))
);

-- changeset abu:create-tasks-indexes
-- comment: Создание индексов для задач
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks (status);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks (created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_title ON tasks (title);

-- changeset abu:create-users-table
-- comment: Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    name          VARCHAR(55)           NOT NULL,
    email         VARCHAR(100)          NOT NULL UNIQUE,
    enabled       BOOLEAN               NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(255)          NOT NULL,
    role          VARCHAR(50)           NOT NULL DEFAULT 'USER',

    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN', 'MODERATOR'))
);