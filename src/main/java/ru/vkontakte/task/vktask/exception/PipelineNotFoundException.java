package ru.vkontakte.task.vktask.exception;

import java.util.UUID;

public class PipelineNotFoundException extends PipelineException {

    public PipelineNotFoundException(UUID pipelineId) {
        super("PIPELINE_NOT_FOUND", "Pipeline not found: " + pipelineId);
    }
}
