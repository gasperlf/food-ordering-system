package net.ontopsolutions.food.ordering.system.order.service.messaging.mapper;

import net.ontopsolutions.food.ordering.system.domain.dto.message.PaymentResponse;
import net.ontopsolutions.food.ordering.system.domain.dto.message.RestaurantApprovalResponse;
import net.ontopsolutions.food.ordering.system.domain.valueobject.OrderApprovalStatus;
import net.ontopsolutions.food.ordering.system.domain.valueobject.PaymentStatus;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.*;
import net.ontopsolutions.food.ordering.system.order.service.domain.entity.Order;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderPaidEvent;

import java.util.UUID;

public class OrderMessagingDataMapper {

    public PaymentRequestAvroModel mapOrderCreatedEventAsPaymentRequestAvroModel(OrderCreatedEvent orderCreatedEvent){
        Order order = orderCreatedEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setOrderId(order.getId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderCreatedEvent.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();
    }

    public PaymentRequestAvroModel mapOrderCancelledEventAsPaymentRequestAvroModel(OrderCancelledEvent orderCancelledEvent){
        Order order = orderCancelledEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setOrderId(order.getId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderCancelledEvent.getCreatedAt().toInstant())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .build();
    }

    public RestaurantApprovalRequestAvroModel mapOrderPaidEventAsRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent){
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(order.getId().getValue().toString())
                .setRestaurantId(order.getRestaurantId().getValue().toString())
                .setProducts(order.getItems().stream().map(orderItem -> Product.newBuilder()
                        .setId(orderItem.getProduct().getId().getValue().toString())
                        .setQuantity(orderItem.getQuantity())
                        .build()).toList())
                .setPrice(order.getPrice().getAmount())
                .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();
    }

    public PaymentResponse mapPaymentResponseAvroModelAsPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .orderId(paymentResponseAvroModel.getOrderId())
                .paymentId(paymentResponseAvroModel.getPaymentId())
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .id(paymentResponseAvroModel.getId())
                .paymentStatus(mapPaymentStatusAsPaymentStatus(paymentResponseAvroModel.getPaymentStatus()))
                .price(paymentResponseAvroModel.getPrice())
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .sagaId(paymentResponseAvroModel.getSagaId())
                .build();
    }

   private PaymentStatus mapPaymentStatusAsPaymentStatus(net.ontopsolutions.food.ordering.system.kafka.order.avro.model.PaymentStatus paymentStatus) {
        return PaymentStatus.valueOf(paymentStatus.name());
   }

   public RestaurantApprovalResponse mapRestaurantApprovalResponseAvroModelAsRestaurantApprovalResponse(RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .id(restaurantApprovalResponseAvroModel.getId())
                .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
                .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
                .orderId(restaurantApprovalResponseAvroModel.getOrderId())
                .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
                .orderApprovalStatus(mapOrderApprovalStatusAsOrderApprovalStatus(restaurantApprovalResponseAvroModel.getOrderApprovalStatus()))
                .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
                .build();
   }

    private OrderApprovalStatus mapOrderApprovalStatusAsOrderApprovalStatus(net.ontopsolutions.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus orderApprovalStatus) {
        return OrderApprovalStatus.valueOf(orderApprovalStatus.name());
    }

}
