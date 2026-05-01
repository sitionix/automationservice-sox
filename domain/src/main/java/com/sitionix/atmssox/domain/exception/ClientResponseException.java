package com.sitionix.atmssox.domain.exception;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class ClientResponseException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;
    private final Map<String, List<String>> responseHeaders;

    public ClientResponseException(final int statusCode,
                                   final String responseBody,
                                   final Map<String, List<String>> responseHeaders,
                                   final Throwable cause) {
        super("Client error response: " + statusCode, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.responseHeaders = responseHeaders == null ? Collections.emptyMap() : responseHeaders;
    }

}
