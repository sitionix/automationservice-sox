package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.CreateProjectConversationCommand;
import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.UUID;

public interface CreateProjectConversation {

    ProjectConversationDetails execute(UUID projectId, CreateProjectConversationCommand command);
}
