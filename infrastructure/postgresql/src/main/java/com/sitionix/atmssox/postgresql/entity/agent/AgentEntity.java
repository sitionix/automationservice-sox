package com.sitionix.atmssox.postgresql.entity.agent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "agents",
        indexes = {
                @Index(name = "idx_agents_user_id_updated_at", columnList = "user_id, updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentEntity {

    @Id
    @Column(name = "agent_id", nullable = false)
    private UUID agentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "description", nullable = true, length = 160)
    private String description;

    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false, referencedColumnName = "id")
    private AgentTypeEntity type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false, referencedColumnName = "id")
    private AgentStatusEntity status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
