package com.pictet.adventurebook.exception;

import com.pictet.adventurebook.model.type.GameStatusType;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        log.error("Unhandled exception", e);

        return createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "The request could not be completed."
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";

        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request parameter",
                String.format(
                        "'%s' is not a valid value for parameter '%s'. Expected type '%s'.",
                        e.getValue(), e.getName(), requiredType
                )
        );
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleBookNotFoundException(BookNotFoundException e) {
        return createProblem(HttpStatus.NOT_FOUND, "Book not found", e.getMessage());
    }

    @ExceptionHandler(SectionNotFoundException.class)
    public ProblemDetail handleSectionNotFoundException(SectionNotFoundException e) {
        return createProblem(HttpStatus.NOT_FOUND, "Section not found", e.getMessage());
    }

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ProblemDetail handleGameSessionNotFoundException(GameSessionNotFoundException e) {
        return createProblem(HttpStatus.NOT_FOUND, "Game not found", e.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    public ProblemDetail handleGameAlreadyFinishedException(GameAlreadyFinishedException e) {
        ProblemDetail problem = createProblem(
                HttpStatus.UNPROCESSABLE_CONTENT,
                e.getStatus() == GameStatusType.DEAD ? "Player is dead" : "Game already completed",
                e.getMessage()
        );

        problem.setProperty("status", e.getStatus());

        return problem;
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    public ProblemDetail handleBookAlreadyExistsException(BookAlreadyExistsException e) {
        return createProblem(HttpStatus.CONFLICT, "Book already exists", e.getMessage());
    }

    private static ProblemDetail createProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("about:blank"));
        problem.setTitle(title);

        return problem;
    }
}
