INSERT INTO roles (name) VALUES
                             ('USER'),
                             ('ADMIN'),
                             ('TRAINER');

INSERT INTO users (email, password_hash, first_name, last_name, role_id, is_active, created_at)
VALUES
    ('jan.kowalski@test.com', 'hash123', 'Jan', 'Kowalski', 1, true, NOW()),
    ('anna.nowak@test.com', 'hash123', 'Anna', 'Nowak', 1, true, NOW()),
    ('admin@test.com', 'hash123', 'Admin', 'User', 2, true, NOW()),
    ('trainer@test.com', 'hash123', 'Tomasz', 'Trener', 3, true, NOW());

INSERT INTO exercises (name, muscle_group, description, is_custom, created_by)
VALUES
    ('Wyciskanie sztangi', 'Klatka piersiowa', 'Podstawowe ćwiczenie na klatkę', false, 3),
    ('Przysiady', 'Nogi', 'Podstawowe ćwiczenie na nogi', false, 3),
    ('Martwy ciąg', 'Plecy', 'Ćwiczenie całego ciała', false, 3),
    ('Uginanie bicepsa', 'Ramiona', 'Ćwiczenie izolowane na biceps', false, 2);

INSERT INTO workouts (user_id, name, description, difficulty_level, estimated_duration, created_at)
VALUES
    (1, 'Dzień push', 'Klatka + triceps', 'ŚREDNI', 60, NOW()),
    (2, 'Dzień nóg', 'Trening nóg', 'TRUDNY', 75, NOW());

INSERT INTO workout_sessions (user_id, workout_id, planned_date, completed_date, status, duration)
VALUES
    (1, 1, NOW(), NOW(), 'UKOŃCZONE', 58),
    (2, 2, NOW(), NULL, 'ZAPLANOWANE', NULL);

INSERT INTO workout_exercises (workout_id, exercise_id, sets, reps, weight, duration)
VALUES
    (1, 1, 4, 10, 80, NULL),
    (1, 4, 3, 12, 15, NULL),
    (2, 2, 5, 8, 100, NULL),
    (2, 3, 3, 5, 120, NULL);

INSERT INTO exercise_results (session_id, exercise_id, sets_done, reps_done, weight_used, duration, notes)
VALUES
    (1, 1, 4, 10, 80, NULL, 'Dobry wynik'),
    (1, 4, 3, 12, 15, NULL, 'Lżejszy ciężar'),
    (2, 2, 5, 8, 100, NULL, 'W trakcie realizacji');

INSERT INTO reports (user_id, type, date_from, date_to, generated_at, file_path)
VALUES
    (1, 'PODSUMOWANIE', '2026-01-01', '2026-01-31', NOW(), '/raporty/uzytkownik1.pdf'),
    (2, 'POSTĘPY', '2026-02-01', '2026-02-28', NOW(), '/raporty/uzytkownik2.pdf');
