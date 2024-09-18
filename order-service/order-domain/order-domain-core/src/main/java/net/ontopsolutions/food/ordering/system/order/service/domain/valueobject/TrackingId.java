package net.ontopsolutions.food.ordering.system.order.service.domain.valueobject;

import net.ontopsolutions.food.ordering.system.domain.valueobject.BaseId;

import java.util.UUID;

public class TrackingId extends BaseId <UUID> {
    public TrackingId(UUID id) {
        super(id);
    }
}
