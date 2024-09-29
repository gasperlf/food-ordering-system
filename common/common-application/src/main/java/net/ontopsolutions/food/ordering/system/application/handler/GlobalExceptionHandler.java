package net.ontopsolutions.food.ordering.system.application.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ProblemDetails handleException(Exception exception){
        log.error(exception.getMessage(), exception);
        return ProblemDetails.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("Unexpected error!")
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public ProblemDetails handleException(ValidationException validationException){

        String exceptionMessage;
        if(validationException instanceof ConstraintViolationException){
            exceptionMessage = extractViolationsFromException((ConstraintViolationException)validationException);
        }else {
            exceptionMessage = validationException.getMessage();
        }

        log.error(exceptionMessage, validationException);
        return ProblemDetails.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(exceptionMessage)
                .build();
    }

    private String extractViolationsFromException(ConstraintViolationException constraintViolationException) {
        return  constraintViolationException.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("--"));
    }


}
