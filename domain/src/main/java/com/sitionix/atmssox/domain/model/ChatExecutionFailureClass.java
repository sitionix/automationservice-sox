package com.sitionix.atmssox.domain.model;

public enum ChatExecutionFailureClass {
    OWNERSHIP_VIOLATION,
    CONVERSATION_NOT_FOUND,
    INVALID_LIFECYCLE_STATE,
    IDEMPOTENCY_CONFLICT,
    EXECUTION_ERROR
}
