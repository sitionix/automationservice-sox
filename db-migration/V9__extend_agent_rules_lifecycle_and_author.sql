INSERT INTO agent_rule_statuses (id, description)
VALUES (3, 'PENDING'),
       (4, 'REJECTED');

ALTER TABLE agent_rules
    ADD COLUMN title VARCHAR(255),
    ADD COLUMN content TEXT,
    ADD COLUMN author_type VARCHAR(32);

UPDATE agent_rules
SET title       = text,
    content     = text,
    author_type = 'USER';

ALTER TABLE agent_rules
    ALTER COLUMN title SET NOT NULL,
    ALTER COLUMN content SET NOT NULL,
    ALTER COLUMN author_type SET NOT NULL;

ALTER TABLE agent_rules
    ADD CONSTRAINT chk_agent_rules_author_type CHECK (author_type IN ('USER', 'AI'));

ALTER TABLE agent_rules
    DROP COLUMN text;
