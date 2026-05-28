CREATE TABLE agent_project_flows (
    flow_id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_agent_project_flows_project FOREIGN KEY (project_id)
        REFERENCES agent_projects (project_id) ON DELETE CASCADE,
    CONSTRAINT uq_agent_project_flows_project UNIQUE (project_id)
);

CREATE TABLE agent_project_flow_nodes (
    node_id UUID PRIMARY KEY,
    flow_id UUID NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    reference_id UUID,
    position_x DOUBLE PRECISION,
    position_y DOUBLE PRECISION,
    design_status VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_agent_project_flow_nodes_flow FOREIGN KEY (flow_id)
        REFERENCES agent_project_flows (flow_id) ON DELETE CASCADE
);

CREATE TABLE agent_project_flow_edges (
    edge_id UUID PRIMARY KEY,
    flow_id UUID NOT NULL,
    source_node_id UUID NOT NULL,
    target_node_id UUID NOT NULL,
    edge_type VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_agent_project_flow_edges_flow FOREIGN KEY (flow_id)
        REFERENCES agent_project_flows (flow_id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_project_flow_edges_source_node FOREIGN KEY (source_node_id)
        REFERENCES agent_project_flow_nodes (node_id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_project_flow_edges_target_node FOREIGN KEY (target_node_id)
        REFERENCES agent_project_flow_nodes (node_id) ON DELETE CASCADE,
    CONSTRAINT chk_agent_project_flow_edges_no_self_loop CHECK (source_node_id <> target_node_id),
    CONSTRAINT uq_agent_project_flow_edges UNIQUE (flow_id, source_node_id, target_node_id, edge_type)
);

CREATE INDEX idx_agent_project_flow_nodes_flow_id ON agent_project_flow_nodes (flow_id);
CREATE INDEX idx_agent_project_flow_edges_flow_id ON agent_project_flow_edges (flow_id);
