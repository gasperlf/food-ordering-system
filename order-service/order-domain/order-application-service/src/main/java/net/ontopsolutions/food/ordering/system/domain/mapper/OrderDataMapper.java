package net.ontopsolutions.food.ordering.system.domain.mapper;

import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderCommand;
import net.ontopsolutions.food.ordering.system.domain.dto.create.CreateOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.dto.create.OrderAddress;
import net.ontopsolutions.food.ordering.system.domain.dto.track.TrackOrderResponse;
import net.ontopsolutions.food.ordering.system.domain.valueobject.*;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.OrderItem;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Product;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Restaurant;
import net.ontopsolutions.food.ordering.system.order.service.domain.valueobject.Address;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Component
public class OrderDataMapper {

   public Restaurant mapCreateOrderCommandAsRestaurant(CreateOrderCommand command) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(command.getRestaurantId()))
                .products(mapOrderItemsAsProducts(command.getOrderItems()))
                .build();
    }

    private static List<Product> mapOrderItemsAsProducts(List<net.ontopsolutions.food.ordering.system.domain.dto.create.OrderItem> orderItems) {
        return orderItems
                .stream().map(OrderDataMapper::mapOrderItemAsProduct)
                .toList();
    }

    private static Product mapOrderItemAsProduct(net.ontopsolutions.food.ordering.system.domain.dto.create.OrderItem orderItem) {
        return new Product(new ProductId(orderItem.getProductId()));
    }


    public Order mapCreateOrderCommandAsOrder(CreateOrderCommand command) {
        return Order.Builder.builder()
                .customerId(new CustomerId(command.getCustomerId()))
                .restaurantId(new RestaurantId(command.getRestaurantId()))
                .deliveryAddress(mapCreateOrderCommandAsAddress(command.getAddress()))
                .price(new Money(command.getPrice()))
                .items(mapCreateOrderCommandAsItems(command.getOrderItems()))
                .build();
    }

    private List<OrderItem> mapCreateOrderCommandAsItems(@NotNull List<net.ontopsolutions.food.ordering.system.domain.dto.create.OrderItem> orderItems) {
        return orderItems.stream().map(this::mapOrderItemAsOrderItem).toList();
   }

   private OrderItem mapOrderItemAsOrderItem(net.ontopsolutions.food.ordering.system.domain.dto.create.OrderItem orderItem) {
       return OrderItem.Builder.builder()
               .product(new Product(new ProductId(orderItem.getProductId())))
               .price(new Money(orderItem.getPrice()))
               .quantity(orderItem.getQuantity())
               .subTotal(new Money(orderItem.getSubTotal()))
               .build();
   }

    public Address mapCreateOrderCommandAsAddress(OrderAddress orderAddress) {
       return new Address(UUID.randomUUID(),
               orderAddress.getStreet(),
               orderAddress.getZipCode(),
               orderAddress.getCity());
    }

    public CreateOrderResponse mapOrderAsCreateOrderResponse(Order order, String message) {
       return CreateOrderResponse.builder()
               .orderTrackingId(order.getTrackingId().getValue())
               .orderStatus(order.getOrderStatus())
               .message(message)
               .build();
    }

    public TrackOrderResponse mapOrderAsTrackOrderResponse(Order order) {
       return TrackOrderResponse.builder()
               .orderTrackingId(order.getTrackingId().getValue())
               .orderStatus(order.getOrderStatus())
               .failureMessages(order.getFailureMessages())
               .build();
    }
}
