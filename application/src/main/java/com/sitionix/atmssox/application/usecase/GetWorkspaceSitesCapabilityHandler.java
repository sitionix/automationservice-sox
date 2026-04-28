package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionCommand;
import com.sitionix.atmssox.domain.model.capability.CapabilityExecutionResult;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemas;
import com.sitionix.atmssox.domain.model.capability.CapabilityName;
import com.sitionix.atmssox.domain.model.capability.WorkspaceSitesArg;
import com.sitionix.atmssox.domain.usecase.CapabilityHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("getWorkspaceSitesCapabilityHandler")
@RequiredArgsConstructor
public class GetWorkspaceSitesCapabilityHandler implements CapabilityHandler<WorkspaceSitesArg> {

    private final WorkspaceProjectionClient workspaceProjectionClient;

    @Override
    public CapabilityDefinition definition() {
        return new CapabilityDefinition(
                CapabilityName.GET_WORKSPACE_SITES.name(),
                "Returns the current user's workspace sites with identifiers, names, statuses, domains and basic metadata.",
                List.of("site", "sites", "workspace", "list", "status"),
                CapabilityInputSchemas.emptyObject(),
                "List of workspace sites with site identifiers and metadata that can be used by other site capabilities."
        );
    }

    @Override
    public CapabilityExecutionResult execute(final CapabilityExecutionCommand<WorkspaceSitesArg> command) {
        final JsonNode payload = this.workspaceProjectionClient.getWorkspaceSites();
        return new CapabilityExecutionResult(CapabilityName.GET_WORKSPACE_SITES, payload);
    }
}
