package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
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
                .title(AgentRuleTextNormalizer.normalizeRequiredTitle(command.title()))
                .content(AgentRuleTextNormalizer.normalizeRequiredContent(command.content()))
                .status(command.status())
                .authorType(command.authorType())
                .createdAt(now)
                .updatedAt(now)
                .build());
    }
}
