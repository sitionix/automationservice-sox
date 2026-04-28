package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.usecase.AgentExecutionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAgentExecutionHandler implements AgentExecutionHandler<UserAgentExecutionContext> {

    private final CapabilityToolLoopService capabilityToolLoopService;

    private final AutomationCapabilitiesProperties automationCapabilitiesProperties;

    @Override
    public Class<UserAgentExecutionContext> supportedContextType() {
        return UserAgentExecutionContext.class;
    }

    @Override
    public String execute(final Agent agent, final UserAgentExecutionContext context) {
        final String contextInstruction = AgentRuleTextNormalizer.normalizeToEmpty(context.instruction());
        final String fallbackInstruction = AgentRuleTextNormalizer.normalizeToEmpty(agent.getInstruction());
        final String input = AgentRuleTextNormalizer.normalizeToEmpty(context.input());
        if (input.isEmpty()) {
            throw new AgentValidationException("User prompt is empty");
        }
        final String instruction = contextInstruction.isEmpty() ? fallbackInstruction : contextInstruction;
        if (!this.automationCapabilitiesProperties.isEnabled()) {
            return this.capabilityToolLoopService.execute(instruction, input);
        }
        return this.capabilityToolLoopService.execute(instruction, input);
    }
}
