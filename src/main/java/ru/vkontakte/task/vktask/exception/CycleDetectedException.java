package ru.vkontakte.task.vktask.exception;

public class CycleDetectedException extends PipelineException {

    public CycleDetectedException() {
        super("CYCLE_DETECTED", "Dependency creates a cycle");
    }
}
