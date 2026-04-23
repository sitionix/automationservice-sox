package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.DeleteAgentRuleResponse;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.domain.repository.AgentRuleRepository;
import com.sitionix.atmssox.domain.usecase.DeleteAgentRule;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAgentRuleImpl implements DeleteAgentRule {

    private final AgentRuleRepository agentRuleRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public DeleteAgentRuleResponse execute(final UUID agentId, final UUID ruleId) {
        final AgentRule current = this.agentRuleRepository.findByIdAndAgentIdAndUserId(
                ruleId,
                agentId,
                this.authenticatedUserProvider.getUserId()
        ).orElseThrow(() -> new AgentNotFoundException("Agent rule not found"));

        if (current.getStatus() == AgentRuleStatus.DELETED) {
            return DeleteAgentRuleResponse.builder()
                    .status(AgentRuleStatus.DELETED)
                    .build();
        }

        this.agentRuleRepository.save(current.toBuilder()
                .status(current.getStatus().delete())
                .updatedAt(Instant.now())
                .build());

        return DeleteAgentRuleResponse.builder()
                .status(AgentRuleStatus.DELETED)
                .build();
    }
}
