package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;

public record UserAgentExecutionContext(String prompt) implements AgentExecutionContext {
}
