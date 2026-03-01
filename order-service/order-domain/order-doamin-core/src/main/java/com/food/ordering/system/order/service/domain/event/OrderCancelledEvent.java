package com.food.ordering.system.order.service.domain.event;

import java.time.ZonedDateTime;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;

public class OrderCancelledEvent extends OrderEvent {

    private final DomainEventPublisher<OrderCancelledEvent> orderCancelledEventPublisher;

    public OrderCancelledEvent(
            Order order,
            ZonedDateTime createdAt,
            DomainEventPublisher<OrderCancelledEvent> orderCancelledEventPublisher) {
        super(order, createdAt);
        this.orderCancelledEventPublisher = orderCancelledEventPublisher;
    }

    @Override
    public void fire() {
        orderCancelledEventPublisher.publish(this);
    }
}
