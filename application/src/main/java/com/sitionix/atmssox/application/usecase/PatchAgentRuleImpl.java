package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.model.PatchAgentRuleCommand;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.usecase.PatchAgentRule;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatchAgentRuleImpl implements PatchAgentRule {

    private final AgentRuleRepository agentRuleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentRule execute(final UUID agentId, final UUID ruleId, final PatchAgentRuleCommand command) {
        final AgentRule current = this.agentRuleRepository.findByIdAndAgentIdAndUserId(
                ruleId,
                agentId,
                this.authenticatedUserProvider.getUserId()
        ).orElseThrow(() -> new AgentNotFoundException("Agent rule not found"));

        if (current.getStatus() != AgentRuleStatus.ACTIVE) {
            throw new AgentLifecycleTransitionException("Only ACTIVE rule can be updated");
        }

        return this.agentRuleRepository.save(current.toBuilder()
                .text(AgentRuleTextNormalizer.normalizeRequired(command.text()))
                .updatedAt(Instant.now())
                .build());
    }
}
