DELETE FROM chat_executions
WHERE status_id = 5;

DELETE FROM chat_execution_statuses
WHERE id = 5
  AND description = 'DISPATCH_SKIPPED';
