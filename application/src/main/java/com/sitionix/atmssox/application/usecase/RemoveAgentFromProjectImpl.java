package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.RemoveAgentFromProject;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveAgentFromProjectImpl implements RemoveAgentFromProject {

    private final AgentProjectRepository agentProjectRepository;
    private final AgentRepository agentRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public void execute(final UUID projectId, final UUID agentId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));
        this.agentRepository.findVisibleByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        final AgentProjectMember activeMembership = this.agentProjectMemberRepository
                .findActiveByProjectIdAndAgentId(projectId, agentId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project member not found"));

        this.agentProjectMemberRepository.save(activeMembership.toBuilder()
                .status(AgentProjectMemberStatus.DELETED)
                .updatedAt(Instant.now())
                .build());
    }
}
