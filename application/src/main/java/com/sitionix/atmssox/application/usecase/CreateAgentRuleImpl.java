package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.exception.AgentValidationException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.CreateAgentRuleCommand;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.usecase.CreateAgentRule;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAgentRuleImpl implements CreateAgentRule {

    private final AgentRepository agentRepository;
    private final AgentRuleRepository agentRuleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentRule execute(final UUID agentId, final CreateAgentRuleCommand command) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentRepository.findVisibleByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        final Instant now = Instant.now();
        return this.agentRuleRepository.save(AgentRule.builder()
                .id(UUID.randomUUID())
                .agentId(agentId)
                .text(this.normalizeText(command.text()))
                .status(AgentRuleStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private String normalizeText(final String value) {
        final String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new AgentValidationException("Rule text is required");
        }
        return normalized;
    }
}
