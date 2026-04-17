CREATE TABLE agent_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_statuses (id, description)
VALUES (1, 'DRAFT'),
       (2, 'ACTIVE'),
       (3, 'ARCHIVED');

CREATE TABLE agents (
    agent_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(160),
    instruction TEXT,
    status_id BIGINT NOT NULL REFERENCES agent_statuses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
