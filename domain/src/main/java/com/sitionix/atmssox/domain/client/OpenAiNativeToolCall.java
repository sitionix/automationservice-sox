package com.sitionix.atmssox.domain.client;

public record OpenAiNativeToolCall(
        String callId,
        String name,
        String argumentsJson
) {
}
