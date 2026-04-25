package com.sitionix.atmssox.domain.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;

public interface AgentExecutionHandler<T extends AgentExecutionContext> {

    Class<T> supportedContextType();

    String execute(Agent agent, T context);

    default String executeWithContext(final Agent agent, final AgentExecutionContext context) {
        if (!this.supportedContextType().isInstance(context)) {
            throw new AgentValidationException(this.supportedContextType().getSimpleName() + " is required");
        }
        return this.execute(agent, this.supportedContextType().cast(context));
    }
}
