package com.food.ordering.system.order.service.application.rest;

import static org.springframework.http.ResponseEntity.*;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderQuery;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "/orders", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping()
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderCommand createOrderCommand) {
        log.info(
                "Create order for customer: {} at restaurant {}",
                createOrderCommand.getCustomerId(),
                createOrderCommand.getRestaurantId());
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        log.info("Create order response with trackingId: {}", response.getOrderTrackingId());
        return status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{trackingId}")
    public ResponseEntity<TrackOrderResponse> trackOrder(@PathVariable UUID trackingId) {
        log.info("Track order for customer: {} at restaurant {}", trackingId, trackingId);
        TrackOrderResponse response =
                orderApplicationService.trackOrder(
                        TrackOrderQuery.builder().orderTrackingId(trackingId).build());
        log.info("Track order response: {}", response);
        return ok(response);
    }
}
