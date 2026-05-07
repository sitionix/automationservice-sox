CREATE INDEX idx_agent_projects_owner_status_updated_at
    ON agent_projects (owner_user_id, status_id, updated_at DESC);

CREATE INDEX idx_agent_project_members_project_status
    ON agent_project_members (project_id, status_id);

CREATE INDEX idx_agent_project_members_agent_status
    ON agent_project_members (agent_id, status_id);
