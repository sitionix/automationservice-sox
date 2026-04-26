package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.AgentType;
import com.sitionix.atmssox.postgresql.entity.agent.AgentTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentTypeInfraMapper {

    default AgentType asType(final AgentTypeEntity typeEntity) {
        if (isNull(typeEntity)) {
            return null;
        }
        return AgentType.fromId(typeEntity.getId());
    }

    default AgentTypeEntity asTypeEntity(final AgentType type) {
        if (isNull(type)) {
            return null;
        }
        return AgentTypeEntity.builder()
                .id(type.getId())
                .description(type.name())
                .build();
    }
}
