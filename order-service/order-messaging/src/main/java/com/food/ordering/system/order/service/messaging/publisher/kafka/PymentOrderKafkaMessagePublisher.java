package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import org.apache.kafka.clients.producer.RecordMetadata;

import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PymentOrderKafkaMessagePublisher
        implements OrderPaidRestaurantRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;

    @Override
    public void publish(OrderPaidEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Creating OrderPaidEvent with orderId {}", orderId);
        RestaurantApprovalRequestAvroModel request =
                orderMessagingDataMapper.orderPaidEventToRestaurantApprovalRequestAvroModel(
                        domainEvent);
        CompletableFuture<SendResult<String, RestaurantApprovalRequestAvroModel>> send =
                kafkaProducer.send(
                        orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                        orderId,
                        request);

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

        log.info("OrderPaidEvent with orderId {}", orderId);
    }
}
