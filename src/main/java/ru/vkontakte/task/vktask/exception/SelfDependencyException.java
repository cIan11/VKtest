package ru.vkontakte.task.vktask.exception;

public class SelfDependencyException extends PipelineException {

    public SelfDependencyException() {
        super("SELF_DEPENDENCY", "Node cannot depend on itself");
    }
}
