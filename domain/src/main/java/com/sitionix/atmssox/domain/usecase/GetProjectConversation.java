package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.UUID;

/**
 * Retrieves a single project-bound conversation shell by project and conversation identifiers.
 */
public interface GetProjectConversation {

    /**
     * Returns project conversation details when the conversation is visible in the specified project.
     *
     * @param projectId target project identifier
     * @param conversationId project conversation identifier
     * @return project conversation details
     */
    ProjectConversationDetails execute(UUID projectId, UUID conversationId);
}
