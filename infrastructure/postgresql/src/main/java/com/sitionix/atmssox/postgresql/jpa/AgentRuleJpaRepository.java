package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRuleJpaRepository extends JpaRepository<AgentRuleEntity, UUID> {

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndStatusOrderByCreatedAtAsc(UUID agentId,
                                                                                           Long userId,
                                                                                           AgentRuleStatus status);

    Optional<AgentRuleEntity> findByRuleIdAndAgentAgentIdAndAgentUserId(UUID ruleId, UUID agentId, Long userId);
}
