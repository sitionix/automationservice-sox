CREATE INDEX idx_agents_user_id_updated_at
    ON agents (user_id, updated_at DESC);
