package net.ontopsolutions.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.config.OrderServiceConfigData;
import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import net.ontopsolutions.food.ordering.system.kafka.producer.service.KafkaProducer;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import net.ontopsolutions.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelOrderKafkaMessagePublisher implements OrderCancelledPaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderKafkaMessageHelper orderKafkaMessageHelper;

    public CancelOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                            OrderServiceConfigData orderServiceConfigData,
                                            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, OrderKafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }


    @Override
    public void publish(OrderCancelledEvent event) {
        String orderId = event.getOrder().getId().getValue().toString();
        log.info("Received OrderCancelledEvent for order id {}", orderId);

        try {
            PaymentRequestAvroModel paymentRequestAvroModel = orderMessagingDataMapper.mapOrderCancelledEventAsPaymentRequestAvroModel(event);
            kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
                    orderId, paymentRequestAvroModel,
                    orderKafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getPaymentRequestTopicName(), paymentRequestAvroModel,
                            orderId, "PaymentRequestAvroModel"));
            log.info("PaymentRequestAvroModel sent to Kafka for order id {} ", paymentRequestAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending PaymentRequestAvroModel for order id: {} error: {}",
                    orderId, e.getMessage());
        }
    }


}
