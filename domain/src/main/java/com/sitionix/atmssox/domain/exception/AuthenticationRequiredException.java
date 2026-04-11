package com.sitionix.atmssox.domain.exception;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException(final String message) {
        super(message);
    }
}
