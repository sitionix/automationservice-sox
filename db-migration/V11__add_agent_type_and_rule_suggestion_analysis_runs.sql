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

INSERT INTO agents (agent_id, user_id, name, description, instruction, type_id, status_id, created_at, updated_at)
VALUES (
           (
               substr(md5(random()::text || clock_timestamp()::text), 1, 8) || '-' ||
               substr(md5(random()::text || clock_timestamp()::text), 1, 4) || '-' ||
               substr(md5(random()::text || clock_timestamp()::text), 1, 4) || '-' ||
               substr(md5(random()::text || clock_timestamp()::text), 1, 4) || '-' ||
               substr(md5(random()::text || clock_timestamp()::text), 1, 12)
               )::uuid,
           0,
           'Rule Analyzer',
           'Internal system agent for suggested rules analysis',
           'You analyze agent conversations and suggest rules that improve behavior.

Rules must:
- reflect repeated user preferences or corrections
- be specific and actionable
- not be generic ("be helpful")

Return ONLY valid JSON in this format:

{
  "suggestions": [
    {
      "title": "...",
      "content": "...",
      "reason": "..."
    }
  ]
}',
           2,
           2,
           NOW(),
           NOW()
       );
