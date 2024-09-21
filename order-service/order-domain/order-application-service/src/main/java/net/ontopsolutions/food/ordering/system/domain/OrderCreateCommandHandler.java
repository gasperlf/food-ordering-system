package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.mapper.OrderDataMapper;
import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderCreateCommandHandler {

    private final OrderCreateHelper orderCreateHelper;
    private final OrderDataMapper orderDataMapper;
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

    public OrderCreateCommandHandler(OrderCreateHelper orderCreateHelper, OrderDataMapper orderDataMapper, OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher) {
        this.orderCreateHelper = orderCreateHelper;
        this.orderDataMapper = orderDataMapper;
        this.orderCreatedPaymentRequestMessagePublisher = orderCreatedPaymentRequestMessagePublisher;
    }


    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        log.info("Order is created with id {}", orderCreatedEvent.getOrder().getId().getValue());
        orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
        return orderDataMapper.mapOrderAsCreateOrderResponse(orderCreatedEvent.getOrder(), "Order created successfully");
    }


}
