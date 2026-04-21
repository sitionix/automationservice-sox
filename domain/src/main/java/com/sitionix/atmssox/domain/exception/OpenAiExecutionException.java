package com.sitionix.atmssox.domain.exception;

public class OpenAiExecutionException extends RuntimeException {

    public OpenAiExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public OpenAiExecutionException(final String message) {
        super(message);
    }
}
