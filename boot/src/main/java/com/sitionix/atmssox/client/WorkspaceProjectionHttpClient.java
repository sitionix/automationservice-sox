package com.sitionix.atmssox.client;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.dto.SiteOverviewDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceProjectionHttpClient implements WorkspaceProjectionClient {

    private final SiteApi siteApi;
    private final ObjectMapper objectMapper;
    private final WorkspaceProjectionClientCallExecutor clientCallExecutor;

    @Override
    public JsonNode getSiteOverview(final Long userId, final UUID siteId) {
        this.validateUserId(userId);
        return this.clientCallExecutor.execute(() -> {
            final SiteOverviewDTO response = this.siteApi.getSiteOverview(siteId);
            return this.objectMapper.valueToTree(response);
        });
    }

    private void validateUserId(final Long userId) {
        if (userId == null || userId <= 0) {
            throw new AgentValidationException("userId is required");
        }
    }
}
