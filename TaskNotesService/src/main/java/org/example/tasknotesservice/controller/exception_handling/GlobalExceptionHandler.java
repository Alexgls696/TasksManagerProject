package org.example.tasknotesservice.controller.exception_handling;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail>handleBindException(WebExchangeBindException exception, Locale locale) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST
                ,messageSource.getMessage("validation.errors.bad_request",new Object[0],"validation.errors.bad_request",locale));
        problemDetail.setProperty("errors",exception.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage).toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }
}
