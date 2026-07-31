CREATE TABLE pipelines (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pipeline_nodes (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_pipeline_nodes_pipeline
        FOREIGN KEY (pipeline_id) REFERENCES pipelines (id) ON DELETE CASCADE,
    CONSTRAINT uq_pipeline_nodes_pipeline_name
        UNIQUE (pipeline_id, name),
    CONSTRAINT uq_pipeline_nodes_id_pipeline
        UNIQUE (id, pipeline_id)
);

CREATE TABLE pipeline_edges (
    id UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL,
    source_node_id UUID NOT NULL,
    target_node_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_pipeline_edges_pipeline
        FOREIGN KEY (pipeline_id) REFERENCES pipelines (id) ON DELETE CASCADE,
    CONSTRAINT fk_pipeline_edges_source_node
        FOREIGN KEY (source_node_id, pipeline_id) REFERENCES pipeline_nodes (id, pipeline_id) ON DELETE CASCADE,
    CONSTRAINT fk_pipeline_edges_target_node
        FOREIGN KEY (target_node_id, pipeline_id) REFERENCES pipeline_nodes (id, pipeline_id) ON DELETE CASCADE,
    CONSTRAINT chk_pipeline_edges_not_self
        CHECK (source_node_id <> target_node_id),
    CONSTRAINT uq_pipeline_edges_dependency
        UNIQUE (pipeline_id, source_node_id, target_node_id)
);

CREATE INDEX idx_pipeline_nodes_pipeline_id
    ON pipeline_nodes (pipeline_id);

CREATE INDEX idx_pipeline_edges_pipeline_id
    ON pipeline_edges (pipeline_id);

CREATE INDEX idx_pipeline_edges_source_node_id
    ON pipeline_edges (source_node_id);

CREATE INDEX idx_pipeline_edges_target_node_id
    ON pipeline_edges (target_node_id);
