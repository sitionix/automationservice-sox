package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import org.springframework.stereotype.Component;

@Component
public class AgentExecutionService {

    private final AgentExecutionHandlerRegistry agentExecutionHandlerRegistry;

    public AgentExecutionService(final AgentExecutionHandlerRegistry agentExecutionHandlerRegistry) {
        this.agentExecutionHandlerRegistry = agentExecutionHandlerRegistry;
    }

    public String execute(final Agent agent, final AgentExecutionContext context) {
        return this.agentExecutionHandlerRegistry.getHandler(agent.getType()).execute(agent, context);
    }
}
