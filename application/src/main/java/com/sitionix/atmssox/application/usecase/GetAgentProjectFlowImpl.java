package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProjectFlow;
import com.sitionix.atmssox.domain.repository.AgentProjectFlowRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectFlow;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentProjectFlowImpl implements GetAgentProjectFlow {

    private final AgentProjectRepository agentProjectRepository;
    private final AgentProjectFlowRepository agentProjectFlowRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public AgentProjectFlow execute(final UUID projectId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        return this.agentProjectFlowRepository.findByProjectId(projectId)
                .orElseGet(() -> AgentProjectFlow.builder()
                        .projectId(projectId)
                        .flowId(null)
                        .nodes(List.of())
                        .edges(List.of())
                        .build());
    }
}
