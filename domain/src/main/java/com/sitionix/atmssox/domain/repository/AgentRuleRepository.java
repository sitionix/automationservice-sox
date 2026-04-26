package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.AgentRule;
import com.sitionix.atmssox.domain.model.AgentRuleAuthorType;
import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for automation agent rules.
 */
public interface AgentRuleRepository {

    /**
     * Stores one agent rule.
     *
     * @param rule rule to persist.
     * @return persisted state.
     */
    AgentRule save(AgentRule rule);

    /**
     * Returns rules for one agent by status ordered by creation time ascending.
     *
     * @param agentId agent identifier.
     * @param userId owner identifier.
     * @param status rule status filter.
     * @return persisted rules.
     */
    List<AgentRule> findAllByAgentIdAndUserIdAndFiltersOrderByCreatedAtAsc(UUID agentId,
                                                                            Long userId,
                                                                            AgentRuleStatus status,
                                                                            AgentRuleAuthorType authorType);

    /**
     * Finds one rule for one owned agent.
     *
     * @param ruleId rule identifier.
     * @param agentId agent identifier.
     * @param userId owner identifier.
     * @return rule when present.
     */
    Optional<AgentRule> findByIdAndAgentIdAndUserId(UUID ruleId, UUID agentId, Long userId);

    /**
     * Counts rules for one agent by status and author type.
     *
     * @param agentId agent identifier.
     * @param status status filter.
     * @param authorType author type filter.
     * @return matching rules count.
     */
    long countByAgentIdAndStatusAndAuthorType(UUID agentId, AgentRuleStatus status, AgentRuleAuthorType authorType);

    long countByAgentIdAndAuthorTypeAndCreatedAtBetween(UUID agentId,
                                                        AgentRuleAuthorType authorType,
                                                        Instant startInclusive,
                                                        Instant endExclusive);

    Optional<Instant> findLastCreatedAtByAgentIdAndAuthorType(UUID agentId, AgentRuleAuthorType authorType);
}
