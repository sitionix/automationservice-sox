package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.usecase.GetAgentProjectAgents;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAgentProjectAgentsImpl implements GetAgentProjectAgents {

    private final AgentProjectRepository agentProjectRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAgent> execute(final UUID projectId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));
        return this.agentProjectMemberRepository.findVisibleProjectAgents(projectId, userId);
    }
}
