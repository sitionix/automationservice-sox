package com.sitionix.atmssox.domain.exception;

public class AgentNotFoundException extends RuntimeException {

    public AgentNotFoundException(final String message) {
        super(message);
    }
}
