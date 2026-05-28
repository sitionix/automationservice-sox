package com.sitionix.atmssox.postgresql.entity.flow;

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
@Table(name = "agent_project_flow_nodes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentProjectFlowNodeEntity {

    @Id
    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flow_id", nullable = false, referencedColumnName = "flow_id")
    private AgentProjectFlowEntity flow;

    @Column(name = "node_type", nullable = false, length = 64)
    private String nodeType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "design_status", length = 32)
    private String designStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
