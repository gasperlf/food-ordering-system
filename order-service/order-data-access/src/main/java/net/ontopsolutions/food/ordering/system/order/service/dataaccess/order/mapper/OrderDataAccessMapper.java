package net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.mapper;

import net.ontopsolutions.food.ordering.system.domain.valueobject.*;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import net.ontopsolutions.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.OrderItem;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Product;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.Address;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderDataAccessMapper {

    public OrderEntity mapOrderAsOrderEntity(Order order){
        OrderEntity orderEntity =  OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .address(mapDeliveryAddressAsOrderAddressEntity(order.getDeliveryAddress()))
                .price(order.getPrice().getAmount())
                .items(mapOrderItemsAsOrderItemEntityList(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(mapListMessageAsString(order.getFailureMessages()))
                .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(orderEntity));
        return orderEntity;
    }

    public Order mapOrderEntityAsOrder(OrderEntity orderEntity) {
        Order order = Order.Builder.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .deliveryAddress(mapOrderAddressEnityAsDeliveryAddress(orderEntity.getAddress()))
                .price(new Money(orderEntity.getPrice()))
                .items(mapOrderItemEntitiesAsOrderItems(orderEntity.getItems()))
                .orderStatus(orderEntity.getOrderStatus())
                .failureMessages(mapStringMessageAsStringMessageList(orderEntity.getFailureMessages()))
                .build();

        return order;
    }

    private List<String> mapStringMessageAsStringMessageList(String failureMessages) {
        return !failureMessages.isEmpty() ? Arrays.stream(failureMessages.split(",")).toList(): List.of();
    }

    private List<OrderItem> mapOrderItemEntitiesAsOrderItems(List<OrderItemEntity> items) {
        return items.stream()
                .map(this::mapOrderItemEntityAsOrderItem)
                .toList();
    }

    private OrderItem mapOrderItemEntityAsOrderItem(OrderItemEntity orderItemEntity) {
        return OrderItem.Builder.builder()
                .orderItemId(new OrderItemId(orderItemEntity.getId()))
                .price(new Money(orderItemEntity.getPrice()))
                .subTotal(new Money(orderItemEntity.getSubTotal()))
                .quantity(orderItemEntity.getQuantity())
                .product(new Product(new ProductId(orderItemEntity.getProductId())))
                .build();
    }

    private Address mapOrderAddressEnityAsDeliveryAddress(OrderAddressEntity address) {
        return new Address(address.getId(),
                address.getStreet(),
                address.getZipCode(),
                address.getCity());
    }

    private String mapListMessageAsString(List<String> failureMessages) {
        return failureMessages !=null ? String.join(",", failureMessages): "";
    }

    private List<OrderItemEntity> mapOrderItemsAsOrderItemEntityList(List<OrderItem> items) {
        return items.stream()
                .map(this::mapOrderItemAsOrderItemEntity)
                .toList();
    }

    private OrderItemEntity mapOrderItemAsOrderItemEntity(OrderItem orderItem) {
        return OrderItemEntity.builder()
                .id(orderItem.getId().getValue())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice().getAmount())
                .subTotal(orderItem.getSubTotal().getAmount())
                .productId(orderItem.getProduct().getId().getValue())
                .build();
    }

    private OrderAddressEntity mapDeliveryAddressAsOrderAddressEntity(Address deliveryAddress) {
        return OrderAddressEntity.builder()
                .id(deliveryAddress.getId())
                .street(deliveryAddress.getStreet())
                .zipCode(deliveryAddress.getZipCode())
                .city(deliveryAddress.getCity())
                .build();
    }
}
