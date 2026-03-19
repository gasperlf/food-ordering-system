package com.food.ordering.system.restaurant.service.messaging.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.order.service.domain.valueobject.ProductId;
import com.food.ordering.system.order.service.domain.valueobject.RestaurantOrderStatus;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;

@Component
public class RestaurantMessagingDataMapper {

    public RestaurantApprovalRequest restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel) {
        return RestaurantApprovalRequest.builder()
                .id(restaurantApprovalRequestAvroModel.getId())
                .sagaId(restaurantApprovalRequestAvroModel.getSagaId())
                .restaurantId(restaurantApprovalRequestAvroModel.getRestaurantId())
                .orderId(restaurantApprovalRequestAvroModel.getOrderId())
                .restaurantOrderStatus(
                        RestaurantOrderStatus.valueOf(
                                restaurantApprovalRequestAvroModel
                                        .getRestaurantOrderStatus()
                                        .name()))
                .products(
                        restaurantApprovalRequestAvroModel.getProducts().stream()
                                .map(
                                        avroModel ->
                                                Product.builder()
                                                        .productId(
                                                                new ProductId(
                                                                        UUID.fromString(
                                                                                avroModel.getId())))
                                                        .quantity(avroModel.getQuantity())
                                                        .build())
                                .toList())
                .build();
    }

    public RestaurantApprovalResponseAvroModel
            orderEventPayloadToRestaurantApprovalResponseAvroModel(
                    String sagaId, OrderEventPayload orderEventPayload) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setOrderId(orderEventPayload.getOrderId())
                .setRestaurantId(orderEventPayload.getRestaurantId())
                .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
                .setOrderApprovalStatus(
                        OrderApprovalStatus.valueOf(orderEventPayload.getOrderApprovalStatus()))
                .setFailureMessages(orderEventPayload.getFailureMessages())
                .build();
    }
}
