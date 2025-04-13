package org.example.userservice.exception_handling;

import lombok.RequiredArgsConstructor;
import org.example.userservice.exception.NoSuchUserException;
import org.example.userservice.exception.NotAuthorizedException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;


    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("user_service.validation.error.bad_request", new Object[0],
                        "user_service.validation.error.bad_request", locale));
        problemDetail.setProperty("errors", exception.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }

    @ExceptionHandler(NoSuchUserException.class)
    public ResponseEntity<ProblemDetail> handleNoSuchUserException(NoSuchUserException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("user_service.error.not_found",
                        new Object[0], "user_service.error.not_found", locale));

        problemDetail.setProperty("error", exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ProblemDetail> handleNotAuthorizedException(NotAuthorizedException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                messageSource.getMessage("user_service.error.unauthorized",new Object[0], "user_service.error.unauthorized", locale));
        problemDetail.setProperty("error", exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(problemDetail);
    }
}
