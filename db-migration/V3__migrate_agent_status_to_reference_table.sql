CREATE TABLE agent_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_statuses (id, description)
VALUES (1, 'DRAFT'),
       (2, 'ACTIVE'),
       (3, 'ARCHIVED');

ALTER TABLE agents
    ADD COLUMN status_id BIGINT;

UPDATE agents
SET status_id = CASE status
                    WHEN 'ACTIVE' THEN 2
                    WHEN 'ARCHIVED' THEN 3
                    ELSE 1
    END;

ALTER TABLE agents
    ALTER COLUMN status_id SET NOT NULL;

ALTER TABLE agents
    ADD CONSTRAINT fk_agents_status_id FOREIGN KEY (status_id) REFERENCES agent_statuses (id);

ALTER TABLE agents
    DROP COLUMN status;
