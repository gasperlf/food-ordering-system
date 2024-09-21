package net.ontopsolutions.food.ordering.system.domain;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderQuery;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.ports.input.service.OrderApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Validated
@Service
@Slf4j
class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderCreateCommandHandler createOrderCommandHandler;
    private final OrderTrackCommandHandler orderTrackCommandHandler;

    public OrderApplicationServiceImpl(OrderCreateCommandHandler createOrderCommandHandler,
                                       OrderTrackCommandHandler orderTrackCommandHandler) {
        this.createOrderCommandHandler = createOrderCommandHandler;
        this.orderTrackCommandHandler = orderTrackCommandHandler;
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        return createOrderCommandHandler.createOrder(createOrderCommand);
    }

    @Override
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return orderTrackCommandHandler.trackOrder(trackOrderQuery);
    }
}
