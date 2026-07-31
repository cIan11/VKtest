package ru.vkontakte.task.vktask.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkontakte.task.vktask.entity.PipelineNode;

public interface PipelineNodeRepository extends JpaRepository<PipelineNode, UUID> {

    List<PipelineNode> findAllByPipelineIdOrderByCreatedAtAsc(UUID pipelineId);

    Optional<PipelineNode> findByIdAndPipelineId(UUID id, UUID pipelineId);

    boolean existsByPipelineIdAndName(UUID pipelineId, String name);
}
