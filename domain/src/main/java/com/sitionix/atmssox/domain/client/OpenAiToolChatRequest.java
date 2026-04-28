package com.sitionix.atmssox.domain.client;

import java.util.List;

public record OpenAiToolChatRequest(
        String instruction,
        String input,
        String previousResponseId,
        List<OpenAiNativeToolDefinition> tools,
        List<OpenAiNativeToolResult> toolResults
) {
}
