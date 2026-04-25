package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentType;

public interface AgentExecutionHandler<T extends AgentExecutionContext> {

    AgentType supportedAgentType();

    Class<T> supportedContextType();

    String execute(Agent agent, T context);

    default String executeWithContext(final Agent agent, final AgentExecutionContext context) {
        if (!this.supportedContextType().isInstance(context)) {
            throw new AgentValidationException(this.supportedContextType().getSimpleName() + " is required");
        }
        return this.execute(agent, this.supportedContextType().cast(context));
    }
}
