ALTER TABLE conversations
    ADD COLUMN project_id UUID NULL REFERENCES agent_projects(project_id);

ALTER TABLE conversations
    ALTER COLUMN last_message_at DROP NOT NULL;

CREATE INDEX idx_conversations_project_updated
    ON conversations(project_id, updated_at DESC);
