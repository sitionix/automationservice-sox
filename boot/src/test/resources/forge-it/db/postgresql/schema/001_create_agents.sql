CREATE TABLE agent_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_statuses (id, description)
VALUES (1, 'DRAFT'),
       (2, 'ACTIVE'),
       (3, 'ARCHIVED'),
       (4, 'DELETED');

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

CREATE TABLE conversations (
    conversation_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(80) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    last_message_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE conversation_participants (
    participant_id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(conversation_id),
    participant_type VARCHAR(32) NOT NULL,
    participant_ref VARCHAR(64) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE conversation_messages (
    message_id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(conversation_id),
    author_type VARCHAR(32) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
