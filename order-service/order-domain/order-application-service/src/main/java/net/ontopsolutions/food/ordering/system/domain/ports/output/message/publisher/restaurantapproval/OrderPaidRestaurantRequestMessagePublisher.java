package net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.restaurantapproval;

import net.ontopsolutions.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderPaidEvent;

public interface OrderPaidRestaurantRequestMessagePublisher extends DomainEventPublisher<OrderPaidEvent> {
}
