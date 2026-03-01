package com.food.ordering.system.order.service.domain;

import java.util.List;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;

public interface OrderDomainService {

    OrderCreatedEvent validateAndInitiateOrder(
            Order order,
            Restaurant restaurant,
            DomainEventPublisher<OrderCreatedEvent> orderCreatedEventPublisher);

    OrderPaidEvent payOrder(
            Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventPublisher);

    void approveOrder(Order order);

    OrderCancelledEvent cancelOrderPayment(
            Order order,
            List<String> failureMessages,
            DomainEventPublisher<OrderCancelledEvent> orderCancelledEventPublisher);

    void cancelOrder(Order order, List<String> failureMessages);
}
