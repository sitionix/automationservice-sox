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

CREATE INDEX idx_conversations_user_last_message
    ON conversations (user_id, last_message_at);

CREATE TABLE conversation_participants (
    participant_id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    participant_type VARCHAR(32) NOT NULL,
    participant_ref VARCHAR(64) NOT NULL,
    joined_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_conversation_participants_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id)
);

CREATE INDEX idx_conversation_participants_conversation
    ON conversation_participants (conversation_id);

CREATE INDEX idx_conversation_participants_lookup
    ON conversation_participants (participant_type, participant_ref, conversation_id);

CREATE TABLE conversation_messages (
    message_id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    author_type VARCHAR(32) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_conversation_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id)
);

CREATE INDEX idx_conversation_messages_order
    ON conversation_messages (conversation_id, created_at);
