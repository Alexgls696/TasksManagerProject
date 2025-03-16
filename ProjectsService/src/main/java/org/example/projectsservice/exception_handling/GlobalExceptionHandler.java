package org.example.projectsservice.exception_handling;

import lombok.RequiredArgsConstructor;
import org.example.projectsservice.exception.NoSuchProjectException;
import org.example.taskservice.exceptions.NoSuchTaskException;
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
    public ResponseEntity<ProblemDetail>handleBindException(BindException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("validation.errors.400",new Object[0],"validation.errors.400",locale));
        problemDetail.setProperty("errors",exception.
                getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .toList());
        return ResponseEntity
                .badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(NoSuchProjectException.class)
    public ResponseEntity<ProblemDetail>handleNoSuchTaskException(NoSuchProjectException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                messageSource.getMessage("project-service.error.not_found",new Object[0],"project-service.error.not_found",locale));
        problemDetail.setProperty("error",exception.getMessage());
        return ResponseEntity.
                status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
