package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentProjectMemberStatus;
import com.sitionix.atmssox.postgresql.entity.member.AgentProjectMemberStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentProjectMemberStatusInfraMapper {

    default AgentProjectMemberStatus asStatus(final AgentProjectMemberStatusEntity statusEntity) {
        if (statusEntity == null) {
            return null;
        }
        return AgentProjectMemberStatus.fromId(statusEntity.getId());
    }

    default AgentProjectMemberStatusEntity asStatusEntity(final AgentProjectMemberStatus status) {
        if (status == null) {
            return null;
        }
        return AgentProjectMemberStatusEntity.builder()
                .id(status.getId())
                .build();
    }
}
