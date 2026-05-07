package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectMember;
import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.domain.model.ProjectAgent;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        AgentProjectMemberStatusInfraMapper.class,
        AgentProjectInfraMapper.class,
        AgentInfraMapper.class
})
public interface AgentProjectMemberInfraMapper {

    @Mapping(target = "projectId", source = "project.projectId")
    @Mapping(target = "agentId", source = "agent.agentId")
    @Mapping(target = "status", source = "status")
    AgentProjectMember asAgentProjectMember(AgentProjectMemberEntity entity);

    @Mapping(target = "project.projectId", source = "projectId")
    @Mapping(target = "agent.agentId", source = "agentId")
    @Mapping(target = "status", source = "status")
    AgentProjectMemberEntity asAgentProjectMemberEntity(AgentProjectMember member);

    @Mapping(target = "id", source = "agent.agentId")
    @Mapping(target = "name", source = "agent.name")
    @Mapping(target = "description", source = "agent.description")
    @Mapping(target = "status", source = "agent.status.id")
    @Mapping(target = "createdAt", source = "agent.createdAt")
    @Mapping(target = "updatedAt", source = "agent.updatedAt")
    @Mapping(target = "membershipId", source = "membershipId")
    @Mapping(target = "attachedAt", source = "createdAt")
    ProjectAgent asProjectAgent(AgentProjectMemberEntity member);

    default AgentStatus asAgentStatus(final Long statusId) {
        return AgentStatus.fromId(statusId);
    }
}
