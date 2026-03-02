package com.food.ordering.system.restaurant.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantApprovalRequestKafkaListener
        implements KafkaConsumer<RestaurantApprovalRequestAvroModel> {

    private final RestaurantApprovalRequestMessageListener restaurantApprovalRequestMessageListener;
    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;

    @Override
    @KafkaListener(
            id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${restaurant-service.restaurant-approval-request-topic-name}")
    public void receive(
            @Payload List<RestaurantApprovalRequestAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info(
                "{} number of restaurant approval responses received with keys {}, partitions {}"
                        + " and offsets {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());
        messages.forEach(
                approvalRequest -> {
                    try {
                        log.info(
                                "Processing order approval for order id: {}",
                                approvalRequest.getOrderId());
                        restaurantApprovalRequestMessageListener.approveOrder(
                                restaurantMessagingDataMapper
                                        .restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(
                                                approvalRequest));
                    } catch (DataAccessException e) {
                        log.error(
                                "Caught optimistic locking exception in"
                                    + " RestaurantApprovalResponseKafkaListener for order id: {}",
                                approvalRequest.getOrderId());
                    } catch (RestaurantNotFoundException e) {
                        log.error(
                                "No restaurant found for restaurant id: {}, and order id: {}",
                                approvalRequest.getRestaurantId(),
                                approvalRequest.getOrderId());
                    }
                });
    }
}
