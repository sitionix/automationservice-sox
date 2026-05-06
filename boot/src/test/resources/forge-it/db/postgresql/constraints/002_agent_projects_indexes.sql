CREATE INDEX idx_agent_projects_owner_status_updated_at
    ON agent_projects (owner_user_id, status_id, updated_at DESC);
