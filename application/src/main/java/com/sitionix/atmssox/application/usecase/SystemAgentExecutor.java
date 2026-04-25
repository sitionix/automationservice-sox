package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import org.springframework.stereotype.Component;

@Component
public class SystemAgentExecutor {

    public String execute(final Agent agent, final SystemAgentContext context) {
        final SystemAgentExecutionType executionType = SystemAgentExecutionType.fromAgentType(agent.getType());
        return executionType.execute(agent, context);
    }
}
