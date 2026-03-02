package com.food.ordering.system.restaurant.service.domain.event;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.valueobject.RestaurantId;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public class OrderApprovedEvent extends OrderApprovalEvent {

    private final DomainEventPublisher<OrderApprovedEvent> orderApprovalEventPublisher;

    public OrderApprovedEvent(
            OrderApproval orderApproval,
            RestaurantId restaurantId,
            List<String> failureMessages,
            ZonedDateTime createdAt,
            DomainEventPublisher<OrderApprovedEvent> orderApprovalEventPublisher) {
        super(orderApproval, restaurantId, failureMessages, createdAt);
        this.orderApprovalEventPublisher = orderApprovalEventPublisher;
    }

    @Override
    public void fire() {
        orderApprovalEventPublisher.publish(this);
    }
}
