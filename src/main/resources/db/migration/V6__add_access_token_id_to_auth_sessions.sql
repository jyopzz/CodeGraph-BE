ALTER TABLE auth_sessions
ADD COLUMN access_token_id UUID;

CREATE INDEX idx_auth_sessions_access_token_id
    ON auth_sessions(access_token_id);