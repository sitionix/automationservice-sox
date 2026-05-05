CREATE INDEX idx_agent_projects_owner_status_updated_at
    ON agent_projects (owner_user_id, status, updated_at DESC);
