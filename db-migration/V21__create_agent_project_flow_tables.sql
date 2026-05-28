CREATE TABLE agent_project_flows (
    flow_id UUID PRIMARY KEY,
    project_id UUID NOT NULL UNIQUE REFERENCES agent_projects(project_id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE agent_project_flow_nodes (
    node_id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES agent_project_flows(flow_id),
    node_type VARCHAR(32) NOT NULL,
    reference_id UUID NULL,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    design_status VARCHAR(32) NOT NULL,
    config JSONB NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_agent_project_flow_nodes_node_type CHECK (node_type IN ('USER', 'AGENT')),
    CONSTRAINT chk_agent_project_flow_nodes_design_status CHECK (design_status IN ('ACTIVE', 'DISABLED'))
);

CREATE INDEX idx_agent_project_flow_nodes_flow_id ON agent_project_flow_nodes (flow_id);
CREATE UNIQUE INDEX uq_agent_project_flow_nodes_node_id_flow_id ON agent_project_flow_nodes (node_id, flow_id);

CREATE TABLE agent_project_flow_edges (
    edge_id UUID PRIMARY KEY,
    flow_id UUID NOT NULL REFERENCES agent_project_flows(flow_id),
    source_node_id UUID NOT NULL,
    target_node_id UUID NOT NULL,
    edge_type VARCHAR(32) NOT NULL,
    config JSONB NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_agent_project_flow_edges_edge_type CHECK (edge_type IN ('DEPENDENCY')),
    CONSTRAINT chk_agent_project_flow_edges_source_not_target CHECK (source_node_id <> target_node_id),
    CONSTRAINT fk_agent_project_flow_edges_source_node FOREIGN KEY (source_node_id, flow_id)
        REFERENCES agent_project_flow_nodes(node_id, flow_id),
    CONSTRAINT fk_agent_project_flow_edges_target_node FOREIGN KEY (target_node_id, flow_id)
        REFERENCES agent_project_flow_nodes(node_id, flow_id)
);

CREATE INDEX idx_agent_project_flow_edges_flow_id ON agent_project_flow_edges (flow_id);
