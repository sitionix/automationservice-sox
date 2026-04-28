package com.sitionix.atmssox.client;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.dto.SiteOverviewDTO;
import com.app_afesox.wagssox.client.dto.WorkspaceSitesPageDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceProjectionHttpClient implements WorkspaceProjectionClient {

    private final SiteApi siteApi;
    private final ObjectMapper objectMapper;
    private final DownstreamClientCallExecutor clientCallExecutor;

    @Override
    public JsonNode getSiteOverview(final UUID siteId) {
        return this.clientCallExecutor.execute(() -> {
            final SiteOverviewDTO response = this.siteApi.getSiteOverview(siteId);
            return this.objectMapper.valueToTree(response);
        });
    }

    @Override
    public JsonNode getWorkspaceSites() {
        return this.clientCallExecutor.execute(() -> {
            final WorkspaceSitesPageDTO response = this.siteApi.getSites(null, null);
            return this.objectMapper.valueToTree(response);
        });
    }

}
