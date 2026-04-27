package com.sitionix.atmssox.postgresql.mapper;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
                AgentRuleStatusInfraMapper.class,
                AgentRuleAuthorTypeInfraMapper.class
        }
)
public interface AgentRuleInfraMapper {

    @Mapping(target = "ruleId", source = "id")
    @Mapping(target = "agent.agentId", source = "agentId")
    AgentRuleEntity asAgentRuleEntity(AgentRule agentRule);

    @Mapping(target = "id", source = "ruleId")
    @Mapping(target = "agentId", source = "agent.agentId")
    AgentRule asAgentRule(AgentRuleEntity agentRuleEntity);
}
