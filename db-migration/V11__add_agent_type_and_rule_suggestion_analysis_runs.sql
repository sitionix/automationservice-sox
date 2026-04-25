ALTER TABLE agents
    ADD COLUMN type VARCHAR(64) NOT NULL DEFAULT 'USER';

CREATE UNIQUE INDEX uq_agent_single_rule_analyzer
    ON agents (type)
    WHERE type = 'SYSTEM_RULE_ANALYZER';

INSERT INTO agents (agent_id, user_id, name, description, instruction, type, status_id, created_at, updated_at)
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
           'SYSTEM_RULE_ANALYZER',
           2,
           NOW(),
           NOW()
       );

CREATE TABLE agent_rule_analysis_runs (
    analysis_id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    conversation_id UUID NOT NULL,
    user_message_count BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_agent_rule_analysis_runs_agent
        FOREIGN KEY (agent_id) REFERENCES agents (agent_id),
    CONSTRAINT fk_agent_rule_analysis_runs_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (conversation_id)
);

CREATE INDEX idx_agent_rule_analysis_runs_agent_conversation_created_at
    ON agent_rule_analysis_runs (agent_id, conversation_id, created_at DESC);

CREATE INDEX idx_agent_rule_analysis_runs_agent_created_at
    ON agent_rule_analysis_runs (agent_id, created_at);
