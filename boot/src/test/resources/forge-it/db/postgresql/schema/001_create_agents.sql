CREATE TABLE agent_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_statuses (id, description)
VALUES (1, 'DRAFT'),
       (2, 'ACTIVE'),
       (3, 'ARCHIVED'),
       (4, 'DELETED');

CREATE TABLE agent_types (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_types (id, description)
VALUES (1, 'USER'),
       (2, 'SYSTEM_RULE_ANALYZER'),
       (3, 'SYSTEM_CONTEXT_OPTIMIZER');

CREATE TABLE agents (
    agent_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(160),
    instruction TEXT,
    type_id BIGINT NOT NULL REFERENCES agent_types(id),
    status_id BIGINT NOT NULL REFERENCES agent_statuses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_agent_single_rule_analyzer
    ON agents (type_id)
    WHERE type_id = 2;

CREATE UNIQUE INDEX uq_agent_single_context_optimizer
    ON agents (type_id)
    WHERE type_id = 3;

CREATE TABLE agent_rules (
    rule_id UUID PRIMARY KEY,
    agent_id UUID NOT NULL REFERENCES agents(agent_id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_type_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE agent_rule_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_rule_statuses (id, description)
VALUES (1, 'ACTIVE'),
       (2, 'DELETED'),
       (3, 'PENDING'),
       (4, 'REJECTED');

CREATE TABLE agent_rule_author_types (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_rule_author_types (id, description)
VALUES (1, 'USER'),
       (2, 'AI');

ALTER TABLE agent_rules
    ADD CONSTRAINT fk_agent_rules_status_id FOREIGN KEY (status_id) REFERENCES agent_rule_statuses(id);

ALTER TABLE agent_rules
    ADD CONSTRAINT fk_agent_rules_author_type_id FOREIGN KEY (author_type_id) REFERENCES agent_rule_author_types(id);

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

CREATE TABLE conversation_context_snapshots (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(conversation_id),
    summary TEXT NOT NULL,
    message_count_until INTEGER NOT NULL,
    last_message_id_until UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_conversation_context_snapshots_conversation_id
    ON conversation_context_snapshots (conversation_id);

CREATE TABLE chat_execution_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO chat_execution_statuses (id, description)
VALUES (1, 'QUEUED'),
       (2, 'IN_PROGRESS'),
       (3, 'COMPLETED'),
       (4, 'FAILED');

CREATE TABLE chat_execution_failure_classes (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO chat_execution_failure_classes (id, description)
VALUES (1, 'OWNERSHIP_VIOLATION'),
       (2, 'CONVERSATION_NOT_FOUND'),
       (3, 'INVALID_LIFECYCLE_STATE'),
       (4, 'IDEMPOTENCY_CONFLICT'),
       (5, 'EXECUTION_ERROR');

CREATE TABLE chat_executions (
    execution_id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(conversation_id),
    user_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL REFERENCES chat_execution_statuses(id),
    request_message TEXT NOT NULL,
    idempotency_key VARCHAR(255),
    assistant_message_id UUID,
    failure_class_id BIGINT REFERENCES chat_execution_failure_classes(id),
    failure_reason TEXT,
    failure_retryable BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);
