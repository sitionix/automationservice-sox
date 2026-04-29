CREATE TABLE chat_executions (
    execution_id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    request_message TEXT NOT NULL,
    idempotency_key VARCHAR(255),
    assistant_message_id UUID,
    failure_class VARCHAR(64),
    failure_reason TEXT,
    failure_retryable BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT fk_chat_executions_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id)
);

CREATE INDEX idx_chat_executions_agent ON chat_executions (agent_id, execution_id);
CREATE INDEX idx_chat_executions_conversation_idempotency ON chat_executions (conversation_id, idempotency_key);
