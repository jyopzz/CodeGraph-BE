CREATE TABLE agent_configurations (
    agent_id VARCHAR(64) PRIMARY KEY,

    user_id BIGINT NOT NULL,

    name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    protocol VARCHAR(10) NOT NULL DEFAULT 'HTTPS',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,

    CONSTRAINT fk_agent_configurations_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT ck_agent_configurations_port
        CHECK (port BETWEEN 1024 AND 65535),

    CONSTRAINT ck_agent_configurations_protocol
        CHECK (protocol IN ('HTTP', 'HTTPS'))
);

CREATE INDEX idx_agent_configurations_user_id
    ON agent_configurations(user_id);