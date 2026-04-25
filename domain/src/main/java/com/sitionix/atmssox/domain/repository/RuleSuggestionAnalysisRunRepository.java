package com.sitionix.atmssox.domain.repository;

import com.sitionix.atmssox.domain.model.RuleSuggestionAnalysisRun;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for rule suggestion analyzer execution history.
 */
public interface RuleSuggestionAnalysisRunRepository {

    /**
     * Saves one analyzer execution record.
     *
     * @param run execution to save.
     * @return persisted execution state.
     */
    RuleSuggestionAnalysisRun save(RuleSuggestionAnalysisRun run);

    /**
     * Returns latest analyzer execution for one agent and conversation.
     *
     * @param agentId target agent identifier.
     * @param conversationId target conversation identifier.
     * @return latest execution when present.
     */
    Optional<RuleSuggestionAnalysisRun> findLatestByAgentIdAndConversationId(UUID agentId, UUID conversationId);

    /**
     * Counts analyzer executions for one agent in time range [fromInclusive, toExclusive).
     *
     * @param agentId target agent identifier.
     * @param fromInclusive range start.
     * @param toExclusive range end.
     * @return count of executions in range.
     */
    long countByAgentIdAndCreatedAtBetween(UUID agentId, Instant fromInclusive, Instant toExclusive);
}
