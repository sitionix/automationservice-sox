package com.sitionix.atmssox.domain.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * Provides access to workspace projection read endpoints.
 */
public interface WorkspaceProjectionClient {

    /**
     * Loads site overview for a given user and site.
     *
     * @param userId authenticated user identifier.
     * @param siteId site identifier.
     * @return site overview payload.
     */
    JsonNode getSiteOverview(Long userId, UUID siteId);
}
