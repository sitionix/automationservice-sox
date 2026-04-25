CREATE TABLE agent_types (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_types (id, description)
VALUES (1, 'USER'),
       (2, 'SYSTEM_RULE_ANALYZER');

ALTER TABLE agents
    ADD COLUMN type_id BIGINT NOT NULL DEFAULT 1;

ALTER TABLE agents
    ADD CONSTRAINT fk_agents_type_id FOREIGN KEY (type_id) REFERENCES agent_types (id);

CREATE UNIQUE INDEX uq_agent_single_rule_analyzer
    ON agents (type_id)
    WHERE type_id = 2;
