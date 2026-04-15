package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.AgentStatus;
import com.sitionix.atmssox.postgresql.entity.agent.AgentStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentStatusInfraMapper {

    default AgentStatus asStatus(final AgentStatusEntity statusEntity) {
        if (isNull(statusEntity)) {
            return null;
        }
        return AgentStatus.fromId(statusEntity.getId());
    }

    default AgentStatusEntity asStatusEntity(final AgentStatus status) {
        if (isNull(status)) {
            return null;
        }
        return AgentStatusEntity.builder()
                .id(status.getId())
                .description(status.name())
                .build();
    }
}
