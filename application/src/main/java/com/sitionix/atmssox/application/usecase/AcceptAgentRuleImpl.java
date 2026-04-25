package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentLifecycleTransitionException;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AcceptAgentRuleCommand;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.model.AgentRuleTextNormalizer;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.usecase.AcceptAgentRule;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcceptAgentRuleImpl implements AcceptAgentRule {

    private final AgentRuleRepository agentRuleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public AgentRule execute(final UUID agentId, final UUID ruleId, final AcceptAgentRuleCommand command) {
        final AgentRule current = this.agentRuleRepository.findByIdAndAgentIdAndUserId(
                ruleId,
                agentId,
                this.authenticatedUserProvider.getUserId()
        ).orElseThrow(() -> new AgentNotFoundException("Agent rule not found"));

        if (current.getStatus() == AgentRuleStatus.ACTIVE) {
            throw new AgentLifecycleTransitionException("ACTIVE rule cannot be accepted");
        }
        if (current.getStatus() != AgentRuleStatus.PENDING
                && current.getStatus() != AgentRuleStatus.REJECTED
                && current.getStatus() != AgentRuleStatus.DELETED) {
            throw new AgentLifecycleTransitionException("Rule status cannot be accepted");
        }

        final String normalizedTitle = command == null ? null : AgentRuleTextNormalizer.normalizeOptionalTitle(command.title());
        final String normalizedContent = command == null ? null : AgentRuleTextNormalizer.normalizeOptionalContent(command.content());

        return this.agentRuleRepository.save(current.toBuilder()
                .title(normalizedTitle == null ? current.getTitle() : normalizedTitle)
                .content(normalizedContent == null ? current.getContent() : normalizedContent)
                .status(AgentRuleStatus.ACTIVE)
                .updatedAt(Instant.now())
                .build());
    }
}
