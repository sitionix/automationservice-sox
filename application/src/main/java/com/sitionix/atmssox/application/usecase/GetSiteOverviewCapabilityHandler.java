package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.CapabilityName;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("getSiteOverviewCapabilityHandler")
@RequiredArgsConstructor
public class GetSiteOverviewCapabilityHandler implements CapabilityHandler {

    private static final CapabilityDefinition CAPABILITY_DEFINITION = new CapabilityDefinition(
            CapabilityName.GET_SITE_OVERVIEW.name(),
            "Returns overview information for a specific site.",
            List.of("site", "overview", "workspace", "status"),
            buildInputSchema(),
            "Site overview with identity, status, domain/publication-related metadata if available."
    );

    private final WorkspaceProjectionClient workspaceProjectionClient;

    @Override
    public CapabilityDefinition definition() {
        return CAPABILITY_DEFINITION;
    }

    @Override
    public CapabilityExecutionResult execute(final CapabilityExecutionCommand command) {
        final String siteIdRaw = this.extractRequiredSiteId(command.arguments());
        final UUID siteId = this.parseSiteId(siteIdRaw);
        final JsonNode payload = this.workspaceProjectionClient.getSiteOverview(command.userId(), siteId);
        return new CapabilityExecutionResult(CapabilityName.GET_SITE_OVERVIEW, payload);
    }

    private String extractRequiredSiteId(final JsonNode arguments) {
        if (arguments == null || arguments.get("siteId") == null) {
            throw new AgentValidationException("siteId is required");
        }
        final String siteId = arguments.get("siteId").asText(null);
        if (!StringUtils.hasText(siteId)) {
            throw new AgentValidationException("siteId is required");
        }
        return siteId;
    }

    private UUID parseSiteId(final String siteId) {
        try {
            return UUID.fromString(siteId);
        } catch (IllegalArgumentException exception) {
            throw new AgentValidationException("siteId must be a valid UUID");
        }
    }

    private static JsonNode buildInputSchema() {
        final ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("type", "object");
        schema.putArray("required").add("siteId");
        final ObjectNode properties = schema.putObject("properties");
        final ObjectNode siteId = properties.putObject("siteId");
        siteId.put("type", "string");
        siteId.put("description", "Site identifier");
        return schema;
    }
}
