package com.food.ordering.system.order.service.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.OrderId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaHelper {

    private final OrderRepository orderRepository;

    public Order findOrder(String orderId) {
        Optional<Order> byId = orderRepository.findById(new OrderId(UUID.fromString(orderId)));

        if (byId.isEmpty()) {
            log.error("Order id {} not found", orderId);
            throw new OrderNotFoundException("Order id " + orderId + " not found");
        }
        return byId.get();
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
