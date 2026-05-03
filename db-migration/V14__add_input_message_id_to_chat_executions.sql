ALTER TABLE chat_executions
    ADD COLUMN input_message_id UUID;

INSERT INTO conversation_messages (message_id, conversation_id, author_type, author_id, content, created_at)
SELECT ce.execution_id,
       ce.conversation_id,
       'USER',
       ce.user_id::text,
       ce.request_message,
       COALESCE(ce.started_at, ce.created_at)
FROM chat_executions ce
WHERE ce.input_message_id IS NULL;

UPDATE chat_executions ce
SET input_message_id = ce.execution_id
WHERE ce.input_message_id IS NULL;

ALTER TABLE chat_executions
    ALTER COLUMN input_message_id SET NOT NULL;

CREATE UNIQUE INDEX uq_chat_executions_input_message_id
    ON chat_executions (input_message_id);
