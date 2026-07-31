package ru.vkontakte.task.vktask.dto;

import java.util.UUID;

public record EdgeResponse(
        UUID id,
        UUID sourceNodeId,
        UUID targetNodeId
) {
}
