CREATE TABLE agent_projects (
    project_id UUID PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_agent_projects_owner_status_updated_at
    ON agent_projects (owner_user_id, status, updated_at DESC);
