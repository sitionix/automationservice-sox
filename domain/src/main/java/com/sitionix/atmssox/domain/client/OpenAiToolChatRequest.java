package com.sitionix.atmssox.domain.client;

import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import java.util.List;

public record OpenAiToolChatRequest(
        String instruction,
        String input,
        String previousResponseId,
        List<CapabilityDefinition> tools,
        List<OpenAiNativeToolResult> toolResults
) {
}
