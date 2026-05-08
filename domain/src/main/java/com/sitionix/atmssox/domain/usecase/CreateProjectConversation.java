package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.UUID;

/**
 * Creates a project-bound conversation shell for a given project and selected agents.
 */
public interface CreateProjectConversation {

    /**
     * Creates a project conversation and returns its details with participants.
     *
     * @param projectId target project identifier
     * @param command selected agents and creation parameters
     * @return created project conversation details
     */
    ProjectConversationDetails execute(UUID projectId, CreateProjectConversationCommand command);
}
