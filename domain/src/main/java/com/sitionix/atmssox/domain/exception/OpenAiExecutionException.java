package com.sitionix.atmssox.domain.exception;

import lombok.Getter;

@Getter
public class OpenAiExecutionException extends RuntimeException {

    private static final int DEFAULT_HTTP_STATUS = 502;

    private final int httpStatus;

    private final String upstreamType;

    private final String upstreamCode;

    private final String upstreamMessage;

    public OpenAiExecutionException(final int httpStatus,
                                    final String upstreamType,
                                    final String upstreamCode,
                                    final String upstreamMessage,
                                    final Throwable cause) {
        super(upstreamMessage, cause);
        this.httpStatus = httpStatus;
        this.upstreamType = upstreamType;
        this.upstreamCode = upstreamCode;
        this.upstreamMessage = upstreamMessage;
    }

    public OpenAiExecutionException(final int httpStatus,
                                    final String upstreamType,
                                    final String upstreamCode,
                                    final String upstreamMessage) {
        this(httpStatus, upstreamType, upstreamCode, upstreamMessage, null);
    }

    public OpenAiExecutionException(final String message, final Throwable cause) {
        this(DEFAULT_HTTP_STATUS, null, null, message, cause);
    }

    public OpenAiExecutionException(final String message) {
        this(DEFAULT_HTTP_STATUS, null, null, message, null);
    }
}
