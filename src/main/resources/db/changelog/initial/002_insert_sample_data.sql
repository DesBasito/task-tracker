-- liquibase formatted sql

-- changeset abu:insert-sample-data
-- comment: Вставка тестовых данных в таблицу tasks
INSERT INTO tasks (title, description, status, created_at, updated_at)
VALUES ('Изучить Spring Boot', 'Пройти курс по Spring Boot и создать первое приложение', 'IN_PROGRESS',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Настроить базу данных', 'Подключить H2 базу данных к проекту', 'COMPLETED', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('Написать REST API', 'Создать контроллеры для CRUD операций', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Добавить логирование', 'Реализовать логирование запросов и ответов', 'IN_PROGRESS', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('Написать тесты', 'Покрыть код unit и integration тестами', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Создать документацию', 'Написать README и API документацию', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Деплой приложения', 'Развернуть приложение на сервере', 'CANCELLED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Оптимизация производительности', 'Добавить кэширование и оптимизировать запросы к БД', 'PENDING',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

--пароль у всех qwe
INSERT INTO users (name, password_hash, email, role)
VALUES ('Antonio Banderas',  '$2a$12$WB2YUbFcCN0tm44SBcKUjua9yiFBsfB3vW02IjuwzY7HGtlQIKzy2', 'qwe@qwe',
        'USER'),
       ('chip & deil', '$2a$12$WB2YUbFcCN0tm44SBcKUjua9yiFBsfB3vW02IjuwzY7HGtlQIKzy2',
        'qwe@qwe.qwe', 'USER');