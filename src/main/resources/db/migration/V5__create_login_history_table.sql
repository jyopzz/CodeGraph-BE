CREATE TABLE login_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NULL,

    email VARCHAR(255) NOT NULL,

    ip_address VARCHAR(45) NOT NULL,

    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    successful BOOLEAN NOT NULL,

    CONSTRAINT fk_login_history_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_login_history_user_id
    ON login_history(user_id);

CREATE INDEX idx_login_history_email
    ON login_history(email);

CREATE INDEX idx_login_history_attempted_at
    ON login_history(attempted_at);