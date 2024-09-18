package net.ontopsolutions.food.ordering.system.domain.event.publisher;

import net.ontopsolutions.food.ordering.system.domain.event.DomainEvent;

public interface DomainEventPublisher<T extends DomainEvent> {

    void publish(T event);
}
