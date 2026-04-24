CREATE INDEX idx_agents_user_id_updated_at
    ON agents (user_id, updated_at DESC);

CREATE INDEX idx_agent_rules_agent_status
    ON agent_rules (agent_id, status_id);

CREATE INDEX idx_agent_rules_agent_created_at
    ON agent_rules (agent_id, created_at ASC);

CREATE INDEX idx_conversations_user_last_message
    ON conversations (user_id, last_message_at DESC);

CREATE INDEX idx_conversation_participants_conversation
    ON conversation_participants (conversation_id);

CREATE INDEX idx_conversation_participants_lookup
    ON conversation_participants (participant_type, participant_ref, conversation_id);

CREATE INDEX idx_conversation_messages_order
    ON conversation_messages (conversation_id, created_at ASC);
