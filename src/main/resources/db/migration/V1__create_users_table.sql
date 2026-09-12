CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    email_verified_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip VARCHAR(45) NOT NULL,

    updated_at TIMESTAMP NULL,
    updated_ip VARCHAR(45) NULL,

    CONSTRAINT uk_users_email UNIQUE (email)
);