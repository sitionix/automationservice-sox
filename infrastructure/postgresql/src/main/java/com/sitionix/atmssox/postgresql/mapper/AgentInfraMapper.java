package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.Agent;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
                AgentStatusInfraMapper.class,
                AgentTypeInfraMapper.class
        }
)
public interface AgentInfraMapper {

    @Mapping(target = "agentId", source = "id")
    AgentEntity asAgentEntity(Agent agent);

    @Mapping(target = "id", source = "agentId")
    Agent asAgent(AgentEntity agentEntity);
}
