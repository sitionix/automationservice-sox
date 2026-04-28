package com.sitionix.atmssox.domain.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

/**
 * Provides access to workspace projection read endpoints.
 */
public interface WorkspaceProjectionClient {

    /**
     * Loads site overview for a site.
     *
     * @param siteId site identifier.
     * @return site overview payload.
     */
    JsonNode getSiteOverview(UUID siteId);

    /**
     * Loads workspace sites page.
     *
     * @return workspace sites payload.
     */
    JsonNode getWorkspaceSites();
}
