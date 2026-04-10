CREATE TABLE agents (
    agent_id UUID PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(160) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
