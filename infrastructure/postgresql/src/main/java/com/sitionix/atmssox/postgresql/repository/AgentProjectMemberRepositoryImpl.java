package com.sitionix.atmssox.postgresql.repository;

import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.domain.repository.AgentProjectMemberRepository;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberEntity;
import com.sitionix.atmssox.postgresql.jpa.AgentProjectMemberJpaRepository;
import com.sitionix.atmssox.postgresql.mapper.AgentProjectMemberInfraMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AgentProjectMemberRepositoryImpl implements AgentProjectMemberRepository {

    private final AgentProjectMemberJpaRepository agentProjectMemberJpaRepository;
    private final AgentProjectMemberInfraMapper agentProjectMemberInfraMapper;

    @Override
    public AgentProjectMember save(final AgentProjectMember member) {
        final AgentProjectMemberEntity entity = this.agentProjectMemberJpaRepository.save(
                this.agentProjectMemberInfraMapper.asAgentProjectMemberEntity(member)
        );
        return this.agentProjectMemberInfraMapper.asAgentProjectMember(entity);
    }

    @Override
    public Optional<AgentProjectMember> findByProjectIdAndAgentId(final UUID projectId, final UUID agentId) {
        return this.agentProjectMemberJpaRepository.findByProjectIdAndAgentId(projectId, agentId)
                .map(this.agentProjectMemberInfraMapper::asAgentProjectMember);
    }

    @Override
    public Optional<AgentProjectMember> findActiveByProjectIdAndAgentId(final UUID projectId, final UUID agentId) {
        return this.agentProjectMemberJpaRepository.findActiveByProjectIdAndAgentId(projectId, agentId, AgentProjectMemberStatus.ACTIVE.getId())
                .map(this.agentProjectMemberInfraMapper::asAgentProjectMember);
    }

    @Override
    public List<ProjectAgent> findVisibleProjectAgents(final UUID projectId, final Long ownerUserId) {
        return this.agentProjectMemberJpaRepository.findVisibleProjectAgents(
                        projectId,
                        ownerUserId,
                        AgentProjectStatus.DELETED.getId(),
                        AgentStatus.DELETED.getId(),
                        AgentProjectMemberStatus.ACTIVE.getId()
                ).stream()
                .map(this::asProjectAgent)
                .toList();
    }

    private ProjectAgent asProjectAgent(final AgentProjectMemberEntity member) {
        return ProjectAgent.builder()
                .id(member.getAgent().getAgentId())
                .name(member.getAgent().getName())
                .description(member.getAgent().getDescription())
                .status(AgentStatus.fromId(member.getAgent().getStatus().getId()))
                .createdAt(member.getAgent().getCreatedAt())
                .updatedAt(member.getAgent().getUpdatedAt())
                .membershipId(member.getMembershipId())
                .attachedAt(member.getCreatedAt())
                .build();
    }
}
