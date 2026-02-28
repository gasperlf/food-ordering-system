package com.food.ordering.system.application.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorDto> handleException(Exception ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorDto.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message("Unexpected error occurred.")
                        .build()
                );
    }

    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity<ErrorDto> handleException(ValidationException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        ErrorDto.ErrorDtoBuilder builder = ErrorDto.builder();
        if (ex instanceof ConstraintViolationException) {
            String violations =  extractViolationsFromException((ConstraintViolationException) ex);
            log.error("violations: {}" , violations);
            builder.message(violations);
        }else {
            log.error(ex.getMessage(), ex);
            builder.message(ex.getMessage());
        }
        return status(HttpStatus.BAD_REQUEST)
                .body(builder
                        .code(HttpStatus.BAD_REQUEST.value() +"")
                        .build()
                );
    }

    private String extractViolationsFromException(ConstraintViolationException ex) {
        return  ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }
}
