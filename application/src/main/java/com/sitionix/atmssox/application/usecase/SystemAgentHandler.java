package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.model.Agent;

public interface SystemAgentHandler {

    String execute(Agent agent, SystemAgentContext context);
}
