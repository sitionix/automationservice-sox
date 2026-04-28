package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolCall;
import com.sitionix.atmssox.domain.client.OpenAiNativeToolResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.SiteOverviewArg;
import com.sitionix.atmssox.domain.model.capability.WorkspaceSitesArg;
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
            log.info("[CAPABILITY] executing capability={}", capabilityName.name());
            final CapabilityExecutionResult result = switch (capabilityName) {
                case GET_WORKSPACE_SITES -> capabilityName.execute(new CapabilityExecutionCommand<>(
                        this.authenticatedUserProvider.getUserId(),
                        null,
                        UUID.randomUUID(),
                        this.objectMapper.treeToValue(this.payloadCodec.parseArgs(toolCall.argumentsJson()), WorkspaceSitesArg.class)
                ));
                case GET_SITE_OVERVIEW -> capabilityName.execute(new CapabilityExecutionCommand<>(
                        this.authenticatedUserProvider.getUserId(),
                        null,
                        UUID.randomUUID(),
                        this.objectMapper.treeToValue(this.payloadCodec.parseArgs(toolCall.argumentsJson()), SiteOverviewArg.class)
                ));
            };
            log.info("[CAPABILITY] capability executed capability={} success=true", capabilityName.name());
            return new OpenAiNativeToolResult(toolCall.callId(), this.payloadCodec.serializeJsonNode(result.payload()));
        } catch (Exception exception) {
            log.warn("[CAPABILITY] capability failed capability={} reason={}", toolCall.name(), exception.getMessage());
            return new OpenAiNativeToolResult(toolCall.callId(), this.payloadCodec.serializeError("Capability call failed"));
        }
    }
}
