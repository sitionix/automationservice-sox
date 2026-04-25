package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;

public interface AgentExecutionHandler {

    String execute(Agent agent, AgentExecutionContext context);
}
