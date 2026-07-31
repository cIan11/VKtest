package ru.vkontakte.task.vktask.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.vkontakte.task.vktask.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PipelineNotFoundException.class, NodeNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(PipelineException exception) {
        return error(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(SelfDependencyException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(PipelineException exception) {
        return error(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler({DuplicateNodeException.class, DuplicateEdgeException.class, CycleDetectedException.class})
    public ResponseEntity<ErrorResponse> handleConflict(PipelineException exception) {
        return error(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message, Instant.now()));
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, PipelineException exception) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(exception.getCode(), exception.getMessage(), Instant.now()));
    }
}
