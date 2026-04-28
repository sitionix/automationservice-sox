package com.sitionix.atmssox.domain.client;

import java.util.List;

public record OpenAiToolChatResponse(
        String responseId,
        String outputText,
        List<OpenAiNativeToolCall> toolCalls
) {
}
