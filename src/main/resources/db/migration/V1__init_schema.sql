CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    age INTEGER,
    gender VARCHAR(20),
    height_cm DOUBLE PRECISION,
    current_weight_kg DOUBLE PRECISION,
    goal_weight_kg DOUBLE PRECISION,
    activity_level VARCHAR(30) DEFAULT 'MODERATELY_ACTIVE',
    fitness_goal VARCHAR(30) DEFAULT 'MAINTENANCE',
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workout_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    name VARCHAR(255),
    notes TEXT,
    duration_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exercise_logs (
    id BIGSERIAL PRIMARY KEY,
    workout_session_id BIGINT NOT NULL REFERENCES workout_sessions(id),
    exercise_name VARCHAR(255) NOT NULL,
    category VARCHAR(30),
    sets INTEGER,
    reps INTEGER,
    weight_kg DOUBLE PRECISION,
    rpe INTEGER,
    duration INTEGER,
    zone VARCHAR(20),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS food_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    meal_category VARCHAR(30) NOT NULL,
    food_name VARCHAR(255) NOT NULL,
    serving_size DOUBLE PRECISION DEFAULT 1,
    unit VARCHAR(20) DEFAULT 'SERVING',
    calories DOUBLE PRECISION NOT NULL,
    protein_g DOUBLE PRECISION DEFAULT 0,
    carbs_g DOUBLE PRECISION DEFAULT 0,
    fat_g DOUBLE PRECISION DEFAULT 0,
    fiber_g DOUBLE PRECISION DEFAULT 0,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sleep_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    duration_hours DOUBLE PRECISION NOT NULL,
    quality_score INTEGER,
    bedtime TIME,
    wake_time TIME,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS water_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    amount_ml DOUBLE PRECISION NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS weight_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    date DATE NOT NULL,
    weight_kg DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(255),
    category VARCHAR(50),
    calories_per_100g DOUBLE PRECISION,
    protein_per_100g DOUBLE PRECISION,
    carbs_per_100g DOUBLE PRECISION,
    fat_per_100g DOUBLE PRECISION,
    fiber_per_100g DOUBLE PRECISION,
    serving_size_g DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_workout_sessions_user_date ON workout_sessions(user_id, date);
CREATE INDEX IF NOT EXISTS idx_exercise_logs_session ON exercise_logs(workout_session_id);
CREATE INDEX IF NOT EXISTS idx_food_logs_user_date ON food_logs(user_id, date);
CREATE INDEX IF NOT EXISTS idx_sleep_logs_user_date ON sleep_logs(user_id, date);
CREATE INDEX IF NOT EXISTS idx_water_logs_user_date ON water_logs(user_id, date);
CREATE INDEX IF NOT EXISTS idx_weight_logs_user_date ON weight_logs(user_id, date);
