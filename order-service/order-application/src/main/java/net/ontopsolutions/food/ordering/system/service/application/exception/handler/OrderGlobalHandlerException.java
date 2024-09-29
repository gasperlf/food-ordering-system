package net.ontopsolutions.food.ordering.system.service.application.exception.handler;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.application.handler.GlobalExceptionHandler;
import net.ontopsolutions.food.ordering.system.application.handler.ProblemDetails;
import net.ontopsolutions.food.ordering.system.order.service.domain.exception.OrderDomainException;
import net.ontopsolutions.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class OrderGlobalHandlerException extends GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrderDomainException.class)
    public ProblemDetails handleException(OrderDomainException orderDomainException){
        log.error(orderDomainException.getMessage(), orderDomainException);
        return ProblemDetails.builder()
                .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(orderDomainException.getMessage())
                .build();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetails handleException(OrderNotFoundException orderNotFoundException){
        log.error(orderNotFoundException.getMessage(), orderNotFoundException);
        return ProblemDetails.builder()
                .code(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(orderNotFoundException.getMessage())
                .build();
    }
}
