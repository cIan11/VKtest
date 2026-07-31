package ru.vkontakte.task.vktask.exception;

import java.util.UUID;

public class NodeNotFoundException extends PipelineException {

    public NodeNotFoundException(UUID nodeId) {
        super("NODE_NOT_FOUND", "Node not found: " + nodeId);
    }
}
