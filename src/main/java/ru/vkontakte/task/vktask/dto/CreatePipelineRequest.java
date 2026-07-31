package ru.vkontakte.task.vktask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePipelineRequest(
        @NotBlank
        @Size(max = 255)
        String name
) {
}
