package ru.vkontakte.task.vktask.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkontakte.task.vktask.entity.PipelineEdge;

public interface PipelineEdgeRepository extends JpaRepository<PipelineEdge, UUID> {

    List<PipelineEdge> findAllByPipelineIdOrderByCreatedAtAsc(UUID pipelineId);

    boolean existsByPipelineIdAndSourceNodeIdAndTargetNodeId(UUID pipelineId, UUID sourceNodeId, UUID targetNodeId);
}
