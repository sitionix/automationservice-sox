package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.UUID;

public interface GetProjectConversation {

    ProjectConversationDetails execute(UUID projectId, UUID conversationId);
}
