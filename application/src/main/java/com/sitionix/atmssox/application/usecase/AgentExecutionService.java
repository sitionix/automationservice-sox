package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class AgentExecutionService {

    public String execute(final Agent agent, final AgentExecutionContext context) {
        return agent.getType().execute(agent, context);
    }
}
