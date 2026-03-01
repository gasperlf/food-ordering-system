package com.food.ordering.system.order.service.messaging.publisher.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import org.apache.kafka.clients.producer.RecordMetadata;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelOrderKafkaMessagePublisher
        implements OrderCancelledPaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

    @Override
    public void publish(OrderCancelledEvent domainEvent) {
        String orderId = domainEvent.getOrder().getId().getValue().toString();
        log.info("Creating OrderCancelledEvent with orderId {}", orderId);
        PaymentRequestAvroModel request =
                orderMessagingDataMapper.orderCancelledEventToPaymentRequestAvroModel(domainEvent);
        CompletableFuture<SendResult<String, PaymentRequestAvroModel>> send =
                kafkaProducer.send(
                        orderServiceConfigData.getPaymentRequestTopicName(), orderId, request);

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

        log.info("PaymentAvroModel sent to Kafka for order Id {}", orderId);
    }
}
