package net.ontopsolutions.food.ordering.system.service.application.rest;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderQuery;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.ports.input.service.OrderApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = "application/vnd.api.v1+json")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderCommand createOrderCommand) {
        log.info("Creating order for customer {} at restaurant {}",
                createOrderCommand.getCustomerId(), createOrderCommand.getRestaurantId());
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        log.info("Order created with tracking id {}", createOrderResponse.getOrderTrackingId());
        return ResponseEntity.ok(createOrderResponse);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<TrackOrderResponse> getOrderByTrackingId(@PathVariable("trackingId") UUID trackingId){


        TrackOrderQuery trackOrderQuery = TrackOrderQuery.builder()
                .orderTrackingId(trackingId)
                .build();

        TrackOrderResponse trackOrderResponse = orderApplicationService.trackOrder(trackOrderQuery);
        log.info("Returning order with trackingId {}", trackOrderResponse.getOrderTrackingId());

        return ResponseEntity.ok(trackOrderResponse);
    }
}
