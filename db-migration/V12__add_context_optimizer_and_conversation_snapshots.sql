INSERT INTO agent_types (id, description)
VALUES (3, 'SYSTEM_CONTEXT_OPTIMIZER');

CREATE UNIQUE INDEX uq_agent_single_context_optimizer
    ON agents (type_id)
    WHERE type_id = 3;

CREATE TABLE conversation_context_snapshots (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    summary TEXT NOT NULL,
    message_count_until INTEGER NOT NULL,
    last_message_id_until UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_conversation_context_snapshots_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id)
);

CREATE UNIQUE INDEX uq_conversation_context_snapshots_conversation_id
    ON conversation_context_snapshots (conversation_id);
