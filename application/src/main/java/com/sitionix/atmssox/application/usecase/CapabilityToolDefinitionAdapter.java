package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CapabilityToolDefinitionAdapter {

    private final ObjectMapper objectMapper;

    public OpenAiNativeToolDefinition toNativeTool(final CapabilityDefinition definition) {
        return new OpenAiNativeToolDefinition(
                definition.name(),
                definition.description(),
                this.objectMapper.valueToTree(definition.inputSchema()),
                true
        );
    }
}
