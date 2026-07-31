package ru.vkontakte.task.vktask.dto;

import java.util.List;
import java.util.UUID;

public record PipelineGraphResponse(
        UUID id,
        String name,
        List<NodeResponse> nodes,
        List<EdgeResponse> edges
) {
}
