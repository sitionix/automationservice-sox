package com.sitionix.atmssox.postgresql.mapper;

import static java.util.Objects.isNull;

import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleAuthorTypeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AgentRuleAuthorTypeInfraMapper {

    default AgentRuleAuthorType asAuthorType(final AgentRuleAuthorTypeEntity authorTypeEntity) {
        if (isNull(authorTypeEntity)) {
            return null;
        }
        return AgentRuleAuthorType.fromId(authorTypeEntity.getId());
    }

    default AgentRuleAuthorTypeEntity asAuthorTypeEntity(final AgentRuleAuthorType authorType) {
        if (isNull(authorType)) {
            return null;
        }
        return AgentRuleAuthorTypeEntity.builder()
                .id(authorType.getId())
                .description(authorType.name())
                .build();
    }
}
