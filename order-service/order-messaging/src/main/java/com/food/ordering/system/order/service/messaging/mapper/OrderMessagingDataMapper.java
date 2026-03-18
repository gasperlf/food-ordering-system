package com.food.ordering.system.order.service.messaging.mapper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.*;
import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.valueobject.OrderApprovalStatus;
import com.food.ordering.system.order.service.domain.valueobject.PaymentStatus;

@Component
public class OrderMessagingDataMapper {

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(
            PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId().toString())
                .sagaId(paymentResponseAvroModel.getSagaId().toString())
                .paymentId(paymentResponseAvroModel.getPaymentId().toString())
                .customerId(paymentResponseAvroModel.getCustomerId().toString())
                .orderId(paymentResponseAvroModel.getOrderId().toString())
                .price(paymentResponseAvroModel.getPrice())
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .paymentStatus(
                        PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse approvalResponseAvroModelToApprovalResponse(
            RestaurantApprovalResponseAvroModel approvalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .id(approvalResponseAvroModel.getId().toString())
                .sagaId(approvalResponseAvroModel.getSagaId().toString())
                .restaurantId(approvalResponseAvroModel.getRestaurantId().toString())
                .orderId(approvalResponseAvroModel.getOrderId().toString())
                .createdAt(approvalResponseAvroModel.getCreatedAt())
                .orderApprovalStatus(
                        OrderApprovalStatus.valueOf(
                                approvalResponseAvroModel.getOrderApprovalStatus().name()))
                .failureMessages(approvalResponseAvroModel.getFailureMessages())
                .build();
    }

    public RestaurantApprovalRequestAvroModel
            orderApprovalEventToRestaurantApprovalRequestAvroModel(
                    String sagaId, OrderApprovalEventPayload orderApprovalEventPayload) {
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.fromString(sagaId))
                .setOrderId(UUID.fromString(orderApprovalEventPayload.getOrderId()))
                .setRestaurantId(UUID.fromString(orderApprovalEventPayload.getRestaurantId()))
                .setRestaurantOrderStatus(
                        RestaurantOrderStatus.valueOf(
                                orderApprovalEventPayload.getRestaurantOrderStatus()))
                .setProducts(
                        orderApprovalEventPayload.getProducts().stream()
                                .map(
                                        orderApprovalEventProduct ->
                                                com.food.ordering.system.kafka.order.avro.model
                                                        .Product.newBuilder()
                                                        .setId(orderApprovalEventProduct.getId())
                                                        .setQuantity(
                                                                orderApprovalEventProduct
                                                                        .getQuantity())
                                                        .build())
                                .collect(Collectors.toList()))
                .setPrice(orderApprovalEventPayload.getPrice())
                .setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
                .build();
    }

    public PaymentRequestAvroModel orderPaymentEventToPaymentRequestAvroModel(
            String sagaId, OrderPaymentEventPayload orderPaymentEventPayload) {
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID())
                .setSagaId(UUID.fromString(sagaId))
                .setCustomerId(UUID.fromString(orderPaymentEventPayload.getCustomerId()))
                .setOrderId(UUID.fromString(orderPaymentEventPayload.getOrderId()))
                .setPrice(orderPaymentEventPayload.getPrice())
                .setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
                .setPaymentOrderStatus(
                        PaymentOrderStatus.valueOf(
                                orderPaymentEventPayload.getPaymentOrderStatus()))
                .build();
    }

    public CustomerModel customerAvroModeltoCustomerModel(CustomerAvroModel customerAvroModel) {
        return CustomerModel.builder()
                .id(customerAvroModel.getId().toString())
                .username(customerAvroModel.getUsername())
                .firstName(customerAvroModel.getFirstName())
                .lastName(customerAvroModel.getLastName())
                .build();
    }
}
