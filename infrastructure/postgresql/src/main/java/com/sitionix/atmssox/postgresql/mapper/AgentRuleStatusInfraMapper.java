package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleStatusEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentRuleStatusInfraMapper {

    default AgentRuleStatus asStatus(final AgentRuleStatusEntity statusEntity) {
        if (isNull(statusEntity)) {
            return null;
        }
        return AgentRuleStatus.fromId(statusEntity.getId());
    }

    default AgentRuleStatusEntity asStatusEntity(final AgentRuleStatus status) {
        if (isNull(status)) {
            return null;
        }
        return AgentRuleStatusEntity.builder()
                .id(status.getId())
                .description(status.name())
                .build();
    }
}
