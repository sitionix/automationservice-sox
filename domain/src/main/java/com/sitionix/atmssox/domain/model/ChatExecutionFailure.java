package com.sitionix.atmssox.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatExecutionFailure {

    ChatExecutionFailureClass failureClass;

    String reason;

    boolean retryable;
}
