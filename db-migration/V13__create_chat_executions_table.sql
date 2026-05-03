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
    conversation_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    status_id BIGINT NOT NULL,
    request_message TEXT NOT NULL,
    idempotency_key VARCHAR(255),
    assistant_message_id UUID,
    failure_class_id BIGINT,
    failure_reason TEXT,
    failure_retryable BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_chat_executions_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id),
    CONSTRAINT fk_chat_executions_status_id FOREIGN KEY (status_id) REFERENCES chat_execution_statuses (id),
    CONSTRAINT fk_chat_executions_failure_class_id FOREIGN KEY (failure_class_id) REFERENCES chat_execution_failure_classes (id)
);

CREATE INDEX idx_chat_executions_agent ON chat_executions (agent_id, execution_id);
CREATE INDEX idx_chat_executions_conversation_idempotency ON chat_executions (conversation_id, idempotency_key);
