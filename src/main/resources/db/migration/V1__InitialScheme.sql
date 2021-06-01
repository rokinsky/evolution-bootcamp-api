CREATE TYPE ROLE AS ENUM ('Student', 'Admin');

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    firstname VARCHAR NOT NULL,
    lastname VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    hash VARCHAR NOT NULL,
    role ROLE NOT NULL DEFAULT 'Student'
);

CREATE TABLE IF NOT EXISTS auth (
    id VARCHAR PRIMARY KEY,
    jwt VARCHAR NOT NULL,
    identity UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry TIMESTAMP NOT NULL,
    last_touched TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR NOT NULL,
    task_message VARCHAR NOT NULL,
    sr_id UUID UNIQUE,
    submission_deadline TIMESTAMP,
    status VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS applications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    sr_id UUID NOT NULL UNIQUE,
    solution_message VARCHAR,
    created_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    status VARCHAR NOT NULL,
    UNIQUE (user_id, course_id)
);
