CREATE TABLE roles (
                       id   BIGSERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       email      VARCHAR(255) NOT NULL UNIQUE,
                       password   VARCHAR(255) NOT NULL,
                       full_name  VARCHAR(255),
                       enabled    BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
                            PRIMARY KEY (user_id, role_id)
);

INSERT INTO roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN');