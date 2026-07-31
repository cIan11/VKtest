package ru.vkontakte.task.vktask.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vkontakte.task.vktask.entity.Pipeline;

public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {
}
