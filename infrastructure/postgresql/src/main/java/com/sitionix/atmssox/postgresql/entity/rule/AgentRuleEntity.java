package com.sitionix.atmssox.postgresql.entity.rule;

import com.sitionix.atmssox.domain.model.AgentRuleStatus;
import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
        name = "agent_rules",
        indexes = {
                @Index(name = "idx_agent_rules_agent_status", columnList = "agent_id, status"),
                @Index(name = "idx_agent_rules_agent_created_at", columnList = "agent_id, created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRuleEntity {

    @Id
    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false, referencedColumnName = "agent_id")
    private AgentEntity agent;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AgentRuleStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
