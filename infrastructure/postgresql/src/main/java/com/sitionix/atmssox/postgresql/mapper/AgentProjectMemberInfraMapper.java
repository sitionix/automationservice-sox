package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectMember;
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
}
