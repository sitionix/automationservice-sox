package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.AgentType;

public interface SystemAgentPromptBuilder {

    AgentType getAgentType();

    String build(SystemAgentContext context);
}
