CREATE TABLE agent_rules (
    rule_id UUID PRIMARY KEY,
    agent_id UUID NOT NULL,
    text TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_agent_rules_agent
        FOREIGN KEY (agent_id) REFERENCES agents (agent_id)
);

CREATE INDEX idx_agent_rules_agent_status
    ON agent_rules (agent_id, status);

CREATE INDEX idx_agent_rules_agent_created_at
    ON agent_rules (agent_id, created_at);
