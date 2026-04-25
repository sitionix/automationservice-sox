package com.sitionix.atmssox.postgresql.entity.analysis;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.conversation.ConversationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agent_rule_analysis_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleSuggestionAnalysisRunEntity {

    @Id
    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false, referencedColumnName = "agent_id")
    private AgentEntity agent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, referencedColumnName = "conversation_id")
    private ConversationEntity conversation;

    @Column(name = "user_message_count", nullable = false)
    private Long userMessageCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
