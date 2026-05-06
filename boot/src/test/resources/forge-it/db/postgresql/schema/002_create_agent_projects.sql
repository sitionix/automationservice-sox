CREATE TABLE agent_project_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_project_statuses (id, description)
VALUES (1, 'ACTIVE'),
       (2, 'ARCHIVED'),
       (3, 'DELETED');

CREATE TABLE agent_projects (
    project_id UUID PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    status_id BIGINT NOT NULL REFERENCES agent_project_statuses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
