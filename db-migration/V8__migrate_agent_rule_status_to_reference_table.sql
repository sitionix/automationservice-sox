CREATE TABLE agent_rule_statuses (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_rule_statuses (id, description)
VALUES (1, 'ACTIVE'),
       (2, 'DELETED');

ALTER TABLE agent_rules
    ADD COLUMN status_id BIGINT;

UPDATE agent_rules
SET status_id = CASE status
                    WHEN 'DELETED' THEN 2
                    ELSE 1
    END;

ALTER TABLE agent_rules
    ALTER COLUMN status_id SET NOT NULL;

ALTER TABLE agent_rules
    ADD CONSTRAINT fk_agent_rules_status_id FOREIGN KEY (status_id) REFERENCES agent_rule_statuses (id);

DROP INDEX idx_agent_rules_agent_status;

ALTER TABLE agent_rules
    DROP COLUMN status;

CREATE INDEX idx_agent_rules_agent_status
    ON agent_rules (agent_id, status_id);
