-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash TEXT NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role_id INTEGER,
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

                       CONSTRAINT fk_users_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id)
);

-- =========================
-- EXERCISES
-- =========================
CREATE TABLE exercises (
                           id SERIAL PRIMARY KEY,
                           name VARCHAR(25) NOT NULL,
                           muscle_group VARCHAR(100),
                           description TEXT,
                           is_custom BOOLEAN DEFAULT FALSE,
                           created_by INTEGER,

                           CONSTRAINT fk_exercises_user
                               FOREIGN KEY (created_by)
                                   REFERENCES users(id)
);

-- =========================
-- WORKOUTS
-- =========================
CREATE TABLE workouts (
                          id SERIAL PRIMARY KEY,
                          user_id INTEGER,
                          name VARCHAR(25) NOT NULL,
                          description TEXT,
                          difficulty_level VARCHAR(50),
                          estimated_duration INTEGER,
                          created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),

                          CONSTRAINT fk_workouts_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
);

-- =========================
-- WORKOUT_EXERCISES
-- =========================
CREATE TABLE workout_exercises (
                                   id SERIAL PRIMARY KEY,
                                   workout_id INTEGER,
                                   exercise_id INTEGER,
                                   sets INTEGER,
                                   reps INTEGER,
                                   weight NUMERIC,
                                   duration INTEGER,

                                   CONSTRAINT fk_we_workout
                                       FOREIGN KEY (workout_id)
                                           REFERENCES workouts(id),

                                   CONSTRAINT fk_we_exercise
                                       FOREIGN KEY (exercise_id)
                                           REFERENCES exercises(id)
);

-- =========================
-- WORKOUT_SESSIONS
-- =========================
CREATE TABLE workout_sessions (
                                  id SERIAL PRIMARY KEY,
                                  user_id INTEGER,
                                  workout_id INTEGER,
                                  planned_date TIMESTAMP WITHOUT TIME ZONE,
                                  completed_date TIMESTAMP WITHOUT TIME ZONE,
                                  status VARCHAR(50),
                                  duration INTEGER,

                                  CONSTRAINT fk_ws_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id),

                                  CONSTRAINT fk_ws_workout
                                      FOREIGN KEY (workout_id)
                                          REFERENCES workouts(id)
);

-- =========================
-- EXERCISE_RESULTS
-- =========================
CREATE TABLE exercise_results (
                                  id SERIAL PRIMARY KEY,
                                  session_id INTEGER,
                                  exercise_id INTEGER,
                                  sets_done INTEGER,
                                  reps_done INTEGER,
                                  weight_used NUMERIC,
                                  duration INTEGER,
                                  notes TEXT,

                                  CONSTRAINT fk_er_session
                                      FOREIGN KEY (session_id)
                                          REFERENCES workout_sessions(id),

                                  CONSTRAINT fk_er_exercise
                                      FOREIGN KEY (exercise_id)
                                          REFERENCES exercises(id)
);

-- =========================
-- REPORTS
-- =========================
CREATE TABLE reports (
                         id SERIAL PRIMARY KEY,
                         user_id INTEGER,
                         type VARCHAR(50),
                         date_from DATE,
                         date_to DATE,
                         generated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
                         file_path TEXT,

                         CONSTRAINT fk_reports_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
);