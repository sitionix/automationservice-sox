package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.usecase.RejectAgentRule;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectAgentRuleImpl implements RejectAgentRule {

    private final AgentRuleRepository agentRuleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentRule execute(final UUID agentId, final UUID ruleId) {
        final AgentRule current = this.agentRuleRepository.findByIdAndAgentIdAndUserId(
                ruleId,
                agentId,
                this.authenticatedUserProvider.getUserId()
        ).orElseThrow(() -> new AgentNotFoundException("Agent rule not found"));

        if (current.getAuthorType() != AgentRuleAuthorType.AI) {
            throw new AgentLifecycleTransitionException("Only AI rule can be rejected");
        }
        if (current.getStatus() != AgentRuleStatus.PENDING) {
            throw new AgentLifecycleTransitionException("Only PENDING AI rule can be rejected");
        }

        return this.agentRuleRepository.save(current.toBuilder()
                .status(AgentRuleStatus.REJECTED)
                .updatedAt(Instant.now())
                .build());
    }
}
