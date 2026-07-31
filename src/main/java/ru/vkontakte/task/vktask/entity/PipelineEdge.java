package ru.vkontakte.task.vktask.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "pipeline_edges")
public class PipelineEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_node_id", nullable = false)
    private PipelineNode sourceNode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_node_id", nullable = false)
    private PipelineNode targetNode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PipelineEdge() {
    }

    public PipelineEdge(Pipeline pipeline, PipelineNode sourceNode, PipelineNode targetNode) {
        this.pipeline = pipeline;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    public UUID getId() {
        return id;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public PipelineNode getSourceNode() {
        return sourceNode;
    }

    public PipelineNode getTargetNode() {
        return targetNode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
