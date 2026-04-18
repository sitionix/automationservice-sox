ALTER TABLE agents
    ALTER COLUMN description DROP NOT NULL;

ALTER TABLE agents
    ADD COLUMN instruction TEXT;
