package com.sitionix.atmssox.postgresql.entity.flow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "node_type", nullable = false)
    private String nodeType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "position_x", nullable = false)
    private Double positionX;

    @Column(name = "position_y", nullable = false)
    private Double positionY;

    @Column(name = "design_status", nullable = false)
    private String designStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
