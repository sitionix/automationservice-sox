package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.usecase.AgentExecutionContext;
import java.util.UUID;

public record UserAgentExecutionContext(UUID agentId, UUID conversationId, String instruction, String input) implements AgentExecutionContext {
}
