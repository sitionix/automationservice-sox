package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.ProjectConversationDetails;
import java.util.List;
import java.util.UUID;

public interface ListProjectConversations {

    List<ProjectConversationDetails> execute(UUID projectId);
}
