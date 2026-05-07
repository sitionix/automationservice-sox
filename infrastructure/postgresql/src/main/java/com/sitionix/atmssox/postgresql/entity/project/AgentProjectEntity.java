package com.sitionix.atmssox.postgresql.entity.project;

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
        name = "agent_projects",
        indexes = {
                @Index(name = "idx_agent_projects_owner_status_updated_at", columnList = "owner_user_id, status_id, updated_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentProjectEntity {

    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "context")
    private String context;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false, referencedColumnName = "id")
    private AgentProjectStatusEntity status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
