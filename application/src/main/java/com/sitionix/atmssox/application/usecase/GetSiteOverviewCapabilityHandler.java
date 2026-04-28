package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemas;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.SiteOverviewArg;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("getSiteOverviewCapabilityHandler")
@RequiredArgsConstructor
public class GetSiteOverviewCapabilityHandler implements CapabilityHandler<SiteOverviewArg> {

    private final WorkspaceProjectionClient workspaceProjectionClient;

    @Override
    public CapabilityDefinition definition() {
        return new CapabilityDefinition(
                CapabilityName.GET_SITE_OVERVIEW.name(),
                "Returns overview information for a specific site.",
                List.of("site", "overview", "workspace", "status"),
                CapabilityInputSchemas.requiredSiteId(),
                "Site overview with identity, status, domain/publication-related metadata if available."
        );
    }

    @Override
    public CapabilityExecutionResult execute(final CapabilityExecutionCommand<SiteOverviewArg> command) {
        if (command == null || command.argNode() == null || command.argNode().siteId() == null) {
            throw new AgentValidationException("Site identifier is required");
        }
        final JsonNode payload = this.workspaceProjectionClient.getSiteOverview(command.userId(), command.argNode().siteId());
        return new CapabilityExecutionResult(CapabilityName.GET_SITE_OVERVIEW, payload);
    }

}
