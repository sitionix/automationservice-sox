package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.rule.AgentRuleEntity;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRuleJpaRepository extends JpaRepository<AgentRuleEntity, UUID> {

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndStatusIdOrderByCreatedAtAsc(UUID agentId,
                                                                                              Long userId,
                                                                                              Long statusId);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndStatusIdAndAuthorTypeOrderByCreatedAtAsc(UUID agentId,
                                                                                                            Long userId,
                                                                                                            Long statusId,
                                                                                                            AgentRuleAuthorType authorType);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdAndAuthorTypeOrderByCreatedAtAsc(UUID agentId,
                                                                                                Long userId,
                                                                                                AgentRuleAuthorType authorType);

    List<AgentRuleEntity> findAllByAgentAgentIdAndAgentUserIdOrderByCreatedAtAsc(UUID agentId, Long userId);

    Optional<AgentRuleEntity> findByRuleIdAndAgentAgentIdAndAgentUserId(UUID ruleId, UUID agentId, Long userId);
}
