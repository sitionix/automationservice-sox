package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProject;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AgentProjectInfraMapper {

    @Mapping(target = "projectId", source = "id")
    AgentProjectEntity asAgentProjectEntity(AgentProject project);

    @Mapping(target = "id", source = "projectId")
    AgentProject asAgentProject(AgentProjectEntity entity);
}
