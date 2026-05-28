package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.model.AgentProjectFlowPalette;
import java.util.UUID;

public interface GetAgentProjectFlowPalette {

    AgentProjectFlowPalette execute(UUID projectId);
}
