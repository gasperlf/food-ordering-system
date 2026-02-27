package com.food.ordering.system.order.service.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    @Override
    public OrderCreatedEvent validateAndInitiateOrder(Order order, Restaurant restaurant) {

        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);

        order.validateOrder();
        order.initializeOrder();

        log.info("Order with id {} has been initialized", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZONE_UTC));
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException(
                    String.format(
                            "Restaurant with id %s is currently not active",
                            restaurant.getId().getValue()));
        }
    }

    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        order.getItems()
                .forEach(
                        orderItem -> {
                            Product product = orderItem.getProduct();
                            restaurant.getProducts().stream()
                                    .filter(resProd -> product.getId().equals(resProd.getId()))
                                    .findFirst()
                                    .ifPresent(
                                            restaurantProduct ->
                                                    product.updateWithConfirmedNameAndPrice(
                                                            restaurantProduct.getName(),
                                                            restaurantProduct.getPrice()));
                        });
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id {} has been payed", order.getId().getValue());

        return new OrderPaidEvent(order, ZonedDateTime.now(ZONE_UTC));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id {} has been approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {

        order.initCancel(failureMessages);
        log.info("Order with id {} is cancelling", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZONE_UTC));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id {} has been cancelled", order.getId().getValue());
    }
}
