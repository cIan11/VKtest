package ru.vkontakte.task.vktask.dto;

import java.util.List;
import java.util.UUID;

public record ExecutionOrderResponse(
        UUID pipelineId,
        List<NodeResponse> nodes
) {
}
