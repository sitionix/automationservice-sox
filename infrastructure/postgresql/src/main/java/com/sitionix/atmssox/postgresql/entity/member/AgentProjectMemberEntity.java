package com.sitionix.atmssox.postgresql.entity.member;

import com.sitionix.atmssox.postgresql.entity.agent.AgentEntity;
import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
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
        name = "agent_project_members",
        indexes = {
                @Index(name = "idx_agent_project_members_project_status", columnList = "project_id, status_id"),
                @Index(name = "idx_agent_project_members_agent_status", columnList = "agent_id, status_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentProjectMemberEntity {

    @Id
    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, referencedColumnName = "project_id")
    private AgentProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agent_id", nullable = false, referencedColumnName = "agent_id")
    private AgentEntity agent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_id", nullable = false, referencedColumnName = "id")
    private AgentProjectMemberStatusEntity status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
