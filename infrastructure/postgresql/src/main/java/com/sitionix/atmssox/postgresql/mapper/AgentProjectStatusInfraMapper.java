package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectStatus;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentProjectStatusInfraMapper {

    default AgentProjectStatus asStatus(final AgentProjectStatusEntity statusEntity) {
        if (statusEntity == null) {
            return null;
        }
        return AgentProjectStatus.fromId(statusEntity.getId());
    }

    default AgentProjectStatusEntity asStatusEntity(final AgentProjectStatus status) {
        if (status == null) {
            return null;
        }
        return AgentProjectStatusEntity.builder()
                .id(status.getId())
                .build();
    }
}
