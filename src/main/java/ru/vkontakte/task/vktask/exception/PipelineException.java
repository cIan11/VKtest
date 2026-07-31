package ru.vkontakte.task.vktask.exception;

public abstract class PipelineException extends RuntimeException {

    private final String code;

    protected PipelineException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
