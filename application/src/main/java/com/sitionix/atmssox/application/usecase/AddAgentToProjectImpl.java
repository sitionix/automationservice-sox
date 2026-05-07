package com.sitionix.atmssox.application.usecase;

import com.sitionix.atmssox.application.security.AuthenticatedUserProvider;
import com.sitionix.atmssox.domain.exception.AgentNotFoundException;
import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.domain.repository.AgentProjectRepository;
import com.sitionix.atmssox.domain.repository.AgentRepository;
import com.sitionix.atmssox.domain.usecase.AddAgentToProject;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddAgentToProjectImpl implements AddAgentToProject {

    private final AgentProjectRepository agentProjectRepository;
    private final AgentRepository agentRepository;
    private final AgentProjectMemberRepository agentProjectMemberRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    @Transactional
    public ProjectAgent execute(final UUID projectId, final UUID agentId) {
        final Long userId = this.authenticatedUserProvider.getUserId();
        this.agentProjectRepository.findVisibleByIdAndOwnerUserId(projectId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent project not found"));

        final Agent agent = this.agentRepository.findVisibleByIdAndUserId(agentId, userId)
                .orElseThrow(() -> new AgentNotFoundException("Agent not found"));

        final Instant now = Instant.now();
        final AgentProjectMember existing = this.agentProjectMemberRepository.findByProjectIdAndAgentId(projectId, agentId)
                .orElse(null);

        if (existing == null) {
            final AgentProjectMember created = this.agentProjectMemberRepository.save(AgentProjectMember.builder()
                    .membershipId(UUID.randomUUID())
                    .projectId(projectId)
                    .agentId(agentId)
                    .status(AgentProjectMemberStatus.ACTIVE)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
            return this.toProjectAgent(agent, created.getMembershipId(), created.getCreatedAt());
        }

        if (existing.getStatus() == AgentProjectMemberStatus.DELETED) {
            this.agentProjectMemberRepository.save(existing.toBuilder()
                    .status(AgentProjectMemberStatus.ACTIVE)
                    .updatedAt(now)
                    .build());
            return this.toProjectAgent(agent, existing.getMembershipId(), now);
        }

        return this.toProjectAgent(agent, existing.getMembershipId(), existing.getCreatedAt());
    }

    private ProjectAgent toProjectAgent(final Agent agent, final UUID membershipId, final Instant attachedAt) {
        return ProjectAgent.builder()
                .id(agent.getId())
                .name(agent.getName())
                .description(agent.getDescription())
                .status(agent.getStatus())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .membershipId(membershipId)
                .attachedAt(attachedAt)
                .build();
    }
}
