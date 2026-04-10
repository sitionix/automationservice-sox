package com.sitionix.atmssox.postgresql.entity.agent;

import com.sitionix.atmssox.domain.model.AgentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
                @Index(name = "idx_agents_updated_at", columnList = "updated_at")
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

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "description", nullable = false, length = 160)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AgentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
