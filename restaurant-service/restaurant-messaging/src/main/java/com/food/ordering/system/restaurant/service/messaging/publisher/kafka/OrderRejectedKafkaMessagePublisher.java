package com.food.ordering.system.restaurant.service.messaging.publisher.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import org.apache.kafka.clients.producer.RecordMetadata;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.restaurant.service.domain.config.RestaurantServiceConfigData;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restaurant.service.messaging.mapper.RestaurantMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRejectedKafkaMessagePublisher implements OrderRejectedMessagePublisher {

    private final RestaurantMessagingDataMapper restaurantMessagingDataMapper;
    private final KafkaProducer<String, RestaurantApprovalResponseAvroModel> kafkaProducer;
    private final RestaurantServiceConfigData restaurantServiceConfigData;

    @Override
    public void publish(OrderRejectedEvent domainEvent) {

        String orderId = domainEvent.getOrderApproval().getOrderId().getValue().toString();
        log.info("Received OrderApprovedEvent for orderId {}", orderId);

        RestaurantApprovalResponseAvroModel responseAvroModel =
                restaurantMessagingDataMapper
                        .orderRejectedEventToRestaurantApprovalRequestAvroModel(domainEvent);
        CompletableFuture<SendResult<String, RestaurantApprovalResponseAvroModel>> send =
                kafkaProducer.send(
                        restaurantServiceConfigData.getRestaurantApprovalResponseTopicName(),
                        orderId,
                        responseAvroModel);
        send.thenAccept(
                        result -> {
                            RecordMetadata recordMetadata = result.getRecordMetadata();
                            log.info(
                                    "Message sent successfully to topic {} partition {} offset {}"
                                            + " with key {}",
                                    recordMetadata.topic(),
                                    recordMetadata.partition(),
                                    recordMetadata.offset(),
                                    orderId);
                        })
                .exceptionally(
                        ex -> {
                            throw new KafkaProducerException(
                                    "Failed to send message with key "
                                            + orderId
                                            + " message: "
                                            + ex.getMessage());
                        });
    }
}
