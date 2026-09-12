-- =========================================================
-- Create refresh tokens
-- =========================================================

CREATE TABLE refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,

    token_id VARCHAR(100) NOT NULL,

    token_hash VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_ip VARCHAR(45) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    revoked_at TIMESTAMP NULL,

    CONSTRAINT uk_refresh_tokens_token_id
        UNIQUE (token_id),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens(expires_at);


-- =========================================================
-- Create authentication sessions
-- =========================================================

CREATE TABLE auth_sessions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id BIGINT NOT NULL,

    session_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_ip VARCHAR(45) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    revoked_at TIMESTAMP NULL,

    CONSTRAINT uk_auth_sessions_session_id
        UNIQUE (session_id),

    CONSTRAINT fk_auth_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_auth_sessions_user_id
    ON auth_sessions(user_id);

CREATE INDEX idx_auth_sessions_expires_at
    ON auth_sessions(expires_at);


-- =========================================================
-- Link refresh tokens to authentication session
-- =========================================================

ALTER TABLE refresh_tokens
ADD COLUMN session_id UUID;

ALTER TABLE refresh_tokens
ADD CONSTRAINT fk_refresh_tokens_session
    FOREIGN KEY (session_id)
    REFERENCES auth_sessions(session_id)
    ON DELETE CASCADE;

CREATE INDEX idx_refresh_tokens_session_id
    ON refresh_tokens(session_id);