package ru.vkontakte.task.vktask.exception;

public class DuplicateNodeException extends PipelineException {

    public DuplicateNodeException(String nodeName) {
        super("DUPLICATE_NODE", "Node already exists in pipeline: " + nodeName);
    }
}
