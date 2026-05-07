CREATE TABLE agent_project_member_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_project_member_statuses (id, description)
VALUES (1, 'ACTIVE'),
       (2, 'DELETED');

CREATE TABLE agent_project_members (
    membership_id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES agent_projects(project_id),
    agent_id UUID NOT NULL REFERENCES agents(agent_id),
    status_id BIGINT NOT NULL REFERENCES agent_project_member_statuses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_agent_project_members_project_agent UNIQUE (project_id, agent_id)
);

CREATE INDEX idx_agent_project_members_project_status
    ON agent_project_members (project_id, status_id);

CREATE INDEX idx_agent_project_members_agent_status
    ON agent_project_members (agent_id, status_id);
