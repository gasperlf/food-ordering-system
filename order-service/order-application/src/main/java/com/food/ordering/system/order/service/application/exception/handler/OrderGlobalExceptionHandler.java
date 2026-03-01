package com.food.ordering.system.order.service.application.exception.handler;

import static org.springframework.http.ResponseEntity.status;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.food.ordering.system.application.handler.ErrorDto;
import com.food.ordering.system.application.handler.GlobalExceptionHandler;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class OrderGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(value = {OrderDomainException.class})
    public ResponseEntity<ErrorDto> handleException(OrderDomainException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorDto.builder()
                                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .build());
    }

    @ExceptionHandler(value = {OrderNotFoundException.class})
    public ResponseEntity<ErrorDto> handleException(OrderNotFoundException ex, WebRequest request) {
        log.error(ex.getMessage(), ex);
        return status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorDto.builder()
                                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                                .message(ex.getMessage())
                                .build());
    }
}
