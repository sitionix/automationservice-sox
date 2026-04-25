package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgentExecutionService {

    private final AgentExecutionHandlerRegistry agentExecutionHandlerRegistry;

    public String execute(final Agent agent, final AgentExecutionContext context) {
        return this.agentExecutionHandlerRegistry.getHandler(agent.getType()).executeWithContext(agent, context);
    }
}
