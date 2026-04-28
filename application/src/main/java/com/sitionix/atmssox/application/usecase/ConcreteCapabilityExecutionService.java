package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConcreteCapabilityExecutionService {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final CapabilityToolPayloadCodec payloadCodec;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public OpenAiNativeToolResult execute(final OpenAiNativeToolCall toolCall) {
        try {
            final CapabilityName capabilityName = CapabilityName.valueOf(toolCall.name());
            log.info("[CAPABILITY] executing capability capability={}", capabilityName.name());
            final Object parsedArg = this.objectMapper.treeToValue(
                    this.payloadCodec.parseArgs(toolCall.argumentsJson()),
                    capabilityName.argType()
            );
            final CapabilityExecutionResult result = capabilityName.execute(new CapabilityExecutionCommand<>(
                    this.authenticatedUserProvider.getUserId(),
                    null,
                    UUID.randomUUID(),
                    parsedArg
            ));
            log.info("[CAPABILITY] capability executed capability={} success=true", capabilityName.name());
            return new OpenAiNativeToolResult(toolCall.callId(), this.payloadCodec.serializeJsonNode(result.payload()));
        } catch (Exception exception) {
            log.warn("[CAPABILITY] capability failed capability={} error={}", toolCall.name(), exception.getMessage());
            return new OpenAiNativeToolResult(toolCall.callId(), this.payloadCodec.serializeError("Capability call failed"));
        }
    }
}
