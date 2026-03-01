package com.food.ordering.system.payment.service.messaging.listener.kafka;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.PaymentOrderStatus;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import com.food.ordering.system.payment.service.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.payment.service.messaging.mapper.PaymentMessagingDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestKafkaListener implements KafkaConsumer<PaymentRequestAvroModel> {

    private final PaymentRequestMessageListener paymentRequestMessageListener;
    private final PaymentMessagingDataMapper paymentMessagingDataMapper;

    @Override
    @KafkaListener(
            id = "${kafka-consumer-config.payment-consumer-group-id}",
            topics = "${payment-service.payment-request-topic-name}")
    public void receive(
            @Payload List<PaymentRequestAvroModel> messages,
            @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
            @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
            @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info(
                "{} number of payment requests received with keys:{}, partitions:{} and offsets:"
                        + " {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());
        messages.forEach(
                paymentRequestAvroModel -> {
                    try {
                        if (PaymentOrderStatus.PENDING
                                == paymentRequestAvroModel.getPaymentOrderStatus()) {
                            log.info(
                                    "Processing payment for order id: {}",
                                    paymentRequestAvroModel.getOrderId());
                            paymentRequestMessageListener.completePayment(
                                    paymentMessagingDataMapper
                                            .paymentRequestAvroModelToPaymentRequestModel(
                                                    paymentRequestAvroModel));
                        } else if (PaymentOrderStatus.CANCELLED
                                == paymentRequestAvroModel.getPaymentOrderStatus()) {
                            log.info(
                                    "Cancel payment for order id: {}",
                                    paymentRequestAvroModel.getOrderId());
                            paymentRequestMessageListener.cancelPayment(
                                    paymentMessagingDataMapper
                                            .paymentRequestAvroModelToPaymentRequestModel(
                                                    paymentRequestAvroModel));
                        }
                    } catch (OptimisticLockingFailureException e) {
                        log.error(
                                "Caught optimistic locking exception in"
                                        + " PaymentResponseKafkaListener for order id: {}",
                                paymentRequestAvroModel.getOrderId());
                    } catch (PaymentNotFoundException e) {
                        log.error(
                                "No order found for order id: {}",
                                paymentRequestAvroModel.getOrderId());
                    }
                });
    }
}
