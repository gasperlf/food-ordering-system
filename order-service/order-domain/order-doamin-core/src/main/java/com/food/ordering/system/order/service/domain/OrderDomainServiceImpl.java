package com.food.ordering.system.order.service.domain;

import static com.food.ordering.system.order.service.domain.DomainConstants.ZONE_UTC;

import java.time.ZonedDateTime;
import java.util.List;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    @Override
    public OrderCreatedEvent validateAndInitiateOrder(
            Order order,
            Restaurant restaurant,
            DomainEventPublisher<OrderCreatedEvent> orderCreatedEventPublisher) {

        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);

        order.validateOrder();
        order.initializeOrder();

        log.info("Order with id {} has been initialized", order.getId().getValue());
        return new OrderCreatedEvent(
                order, ZonedDateTime.now(ZONE_UTC), orderCreatedEventPublisher);
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
    public OrderPaidEvent payOrder(
            Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventPublisher) {
        order.pay();
        log.info("Order with id {} has been payed", order.getId().getValue());

        return new OrderPaidEvent(order, ZonedDateTime.now(ZONE_UTC), orderPaidEventPublisher);
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id {} has been approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(
            Order order,
            List<String> failureMessages,
            DomainEventPublisher<OrderCancelledEvent> orderCancelledEventPublisher) {

        order.initCancel(failureMessages);
        log.info("Order with id {} is cancelling", order.getId().getValue());
        return new OrderCancelledEvent(
                order, ZonedDateTime.now(ZONE_UTC), orderCancelledEventPublisher);
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with id {} has been cancelled", order.getId().getValue());
    }
}
