package com.food.ordering.system.order.service.domain.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.CustomerId;
import com.food.ordering.system.order.service.domain.valueobject.Money;
import com.food.ordering.system.order.service.domain.valueobject.ProductId;
import com.food.ordering.system.order.service.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;

import jakarta.validation.constraints.NotNull;

@Component
public class OrderDataMapper {

    public Restaurant createRestaurant(CreateOrderCommand createOrderCommand) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(
                        createOrderCommand.getItems().stream()
                                .map(
                                        orderItem -> {
                                            return new Product(
                                                    new ProductId(orderItem.getProductId()));
                                        })
                                .toList())
                .build();
    }

    public Order createOrderCommandToOrder(CreateOrderCommand createOrderCommand) {
        return Order.builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .items(orderItemsToOrderItemEntities(createOrderCommand.getItems()))
                .build();
    }

    public CreateOrderResponse createOrderToCreateOrderResponse(Order order, String message) {
        return CreateOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .message(message)
                .build();
    }

    private List<OrderItem> orderItemsToOrderItemEntities(
            @NotNull
                    List<com.food.ordering.system.order.service.domain.dto.create.OrderItem>
                            items) {
        return items.stream()
                .map(
                        orderItem ->
                                OrderItem.builder()
                                        .product(
                                                new Product(
                                                        new ProductId(orderItem.getProductId())))
                                        .price(new Money(orderItem.getPrice()))
                                        .quantity(orderItem.getQuantity())
                                        .subTotal(new Money(orderItem.getSubtotal()))
                                        .build())
                .toList();
    }

    private StreetAddress orderAddressToStreetAddress(@NotNull OrderAddress address) {
        return new StreetAddress(
                UUID.randomUUID(), address.getStreet(), address.getPostalCode(), address.getCity());
    }

    public TrackOrderResponse orderToTrackOrderResponse(Order order) {
        return TrackOrderResponse.builder()
                .orderTrackingId(order.getTrackingId().getValue())
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages())
                .build();
    }
}
