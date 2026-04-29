package com.sitionix.atmssox.domain.exception;

public class AgentAccessDeniedException extends RuntimeException {

    public AgentAccessDeniedException(final String message) {
        super(message);
    }
}
