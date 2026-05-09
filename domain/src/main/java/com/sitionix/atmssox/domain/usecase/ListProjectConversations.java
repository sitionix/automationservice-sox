package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.List;
import java.util.UUID;

/**
 * Lists visible project-bound conversation shells for a project.
 */
public interface ListProjectConversations {

    /**
     * Returns active project conversations available for the current user in the project.
     *
     * @param projectId target project identifier
     * @return list of project conversation details
     */
    List<ProjectConversationDetails> execute(UUID projectId);
}
