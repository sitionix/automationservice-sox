package com.sitionix.atmssox.client;

import com.app_afesox.wagssox.client.api.SiteApi;
import com.app_afesox.wagssox.client.dto.SiteOverviewDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.client.WorkspaceProjectionClient;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

@Component
@RequiredArgsConstructor
public class WorkspaceProjectionHttpClient implements WorkspaceProjectionClient {

    private final SiteApi siteApi;
    private final ObjectMapper objectMapper;

    @Override
    public JsonNode getSiteOverview(final Long userId, final UUID siteId) {
        this.validateUserId(userId);
        try {
            final SiteOverviewDTO response = this.siteApi.getSiteOverview(siteId);
            return this.objectMapper.valueToTree(response);
        } catch (HttpStatusCodeException exception) {
            throw this.mapHttpException(exception);
        }
    }

    private void validateUserId(final Long userId) {
        if (userId == null || userId <= 0) {
            throw new AgentValidationException("userId is required");
        }
    }

    private RuntimeException mapHttpException(final HttpStatusCodeException exception) {
        if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new AgentNotFoundException("Site overview not found");
        }
        if (exception.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new AgentValidationException("Invalid siteId");
        }
        return new IllegalStateException(
                "Workspace projection request failed with status: " + exception.getStatusCode().value(),
                exception
        );
    }
}
