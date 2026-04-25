package com.sitionix.atmssox.postgresql.jpa;

import com.sitionix.atmssox.postgresql.entity.analysis.RuleSuggestionAnalysisRunEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleSuggestionAnalysisRunJpaRepository extends JpaRepository<RuleSuggestionAnalysisRunEntity, UUID> {

    Optional<RuleSuggestionAnalysisRunEntity> findFirstByAgentAgentIdAndConversationConversationIdOrderByCreatedAtDesc(UUID agentId,
                                                                                                                        UUID conversationId);

    long countByAgentAgentIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(UUID agentId, Instant fromInclusive, Instant toExclusive);
}
