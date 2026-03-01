package com.food.ordering.system.payment.service.messaging.publisher.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import org.apache.kafka.clients.producer.RecordMetadata;

import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.producer.exception.KafkaProducerException;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.payment.service.domain.config.PaymentServiceConfigData;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedKafkaMessagePublisher implements PaymentCompletedMessagePublisher {

    private final PaymentMessagingDataMapper paymentMessagingDataMapper;
    private final KafkaProducer<String, PaymentResponseAvroModel> kafkaProducer;
    private final PaymentServiceConfigData paymentServiceConfigData;

    @Override
    public void publish(PaymentCompletedEvent domainEvent) {
        String orderId = domainEvent.getPayment().getOrderId().getValue().toString();

        log.info("Received PaymentCompletedEvent for order id: {} ", orderId);

        PaymentResponseAvroModel responseAvroModel =
                paymentMessagingDataMapper.paymentCompletedEventToPaymentResponseAvroModel(
                        domainEvent);

        CompletableFuture<SendResult<String, PaymentResponseAvroModel>> send =
                kafkaProducer.send(
                        paymentServiceConfigData.getPaymentResponseTopicName(),
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

        log.info("PaymentResponseAvroModel sent to Kafka for order id: {} ", orderId);
    }
}
