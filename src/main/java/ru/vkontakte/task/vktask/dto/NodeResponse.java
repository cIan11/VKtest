package ru.vkontakte.task.vktask.dto;

import java.util.UUID;

public record NodeResponse(
        UUID id,
        String name
) {
}
