package com.food.ordering.system.payment.service.domain.event;

import java.time.ZonedDateTime;
import java.util.Collections;

import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;

public class PaymentCompletedEvent extends PaymentEvent {

    private final DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventPublisher;

    public PaymentCompletedEvent(
            Payment payment,
            ZonedDateTime createdAt,
            DomainEventPublisher<PaymentCompletedEvent> paymentCompletedEventPublisher) {
        super(payment, createdAt, Collections.emptyList());
        this.paymentCompletedEventPublisher = paymentCompletedEventPublisher;
    }

    @Override
    public void fire() {
        paymentCompletedEventPublisher.publish(this);
    }
}
