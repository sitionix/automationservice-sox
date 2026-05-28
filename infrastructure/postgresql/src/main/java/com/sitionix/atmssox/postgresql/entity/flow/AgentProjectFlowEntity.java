package com.sitionix.atmssox.postgresql.entity.flow;

import com.sitionix.atmssox.postgresql.entity.project.AgentProjectEntity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agent_project_flows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentProjectFlowEntity {

    @Id
    @Column(name = "flow_id", nullable = false)
    private UUID flowId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, referencedColumnName = "project_id")
    private AgentProjectEntity project;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
