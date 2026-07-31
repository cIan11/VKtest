package ru.vkontakte.task.vktask.exception;

public class DuplicateEdgeException extends PipelineException {

    public DuplicateEdgeException() {
        super("DUPLICATE_EDGE", "Dependency already exists");
    }
}
