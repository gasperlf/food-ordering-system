package net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.payment;

import net.ontopsolutions.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCreatedEvent;

public interface OrderCreatedPaymentRequestMessagePublisher extends DomainEventPublisher<OrderCreatedEvent> {
}
