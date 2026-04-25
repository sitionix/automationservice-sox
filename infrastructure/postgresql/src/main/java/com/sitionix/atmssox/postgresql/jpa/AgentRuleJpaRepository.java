package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRuleJpaRepository extends JpaRepository<AgentRuleEntity, UUID> {

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(UUID agentId,
                                                                                              Long userId,
                                                                                              Long statusId);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndStatusIdAndAuthorTypeIdOrderByCreatedAtAsc(UUID agentId,
                                                                                                              Long userId,
                                                                                                              Long statusId,
                                                                                                              Long authorTypeId);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndAuthorTypeIdOrderByCreatedAtAsc(UUID agentId,
                                                                                                  Long userId,
                                                                                                  Long authorTypeId);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdOrderByCreatedAtAsc(UUID agentId, Long userId);

    Optional<AgentRuleEntity> findByRuleIdAndAgentAgentIdAndAgentUserId(UUID ruleId, UUID agentId, Long userId);
}
