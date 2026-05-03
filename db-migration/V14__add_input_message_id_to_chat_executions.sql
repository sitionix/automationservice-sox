ALTER TABLE chat_executions
    ADD COLUMN input_message_id UUID;

ALTER TABLE chat_executions
    ALTER COLUMN input_message_id SET NOT NULL;

CREATE UNIQUE INDEX uq_chat_executions_input_message_id
    ON chat_executions (input_message_id);
