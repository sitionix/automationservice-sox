CREATE TABLE agent_rule_author_types (
    id BIGINT PRIMARY KEY,
    description VARCHAR(64) NOT NULL
);

INSERT INTO agent_rule_author_types (id, description)
VALUES (1, 'USER'),
       (2, 'AI');

ALTER TABLE agent_rules
    ADD COLUMN author_type_id BIGINT;

TRUNCATE TABLE agent_rules;

ALTER TABLE agent_rules
    ALTER COLUMN author_type_id SET NOT NULL;

ALTER TABLE agent_rules
    ADD CONSTRAINT fk_agent_rules_author_type_id FOREIGN KEY (author_type_id) REFERENCES agent_rule_author_types(id);

ALTER TABLE agent_rules
    DROP CONSTRAINT IF EXISTS chk_agent_rules_author_type;

ALTER TABLE agent_rules
    DROP COLUMN author_type;
