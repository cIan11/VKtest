package ru.vkontakte.task.vktask.dto;

import java.util.UUID;

public record PipelineResponse(
        UUID id,
        String name
) {
}
