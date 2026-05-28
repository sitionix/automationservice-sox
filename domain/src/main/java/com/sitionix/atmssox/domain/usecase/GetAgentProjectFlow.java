package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import java.util.UUID;

public interface GetAgentProjectFlow {

    AgentProjectFlow execute(UUID projectId);
}
