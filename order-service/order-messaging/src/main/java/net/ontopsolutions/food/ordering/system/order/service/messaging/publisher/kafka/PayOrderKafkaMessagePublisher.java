package net.ontopsolutions.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.config.OrderServiceConfigData;
import net.ontopsolutions.food.ordering.system.domain.ports.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import net.ontopsolutions.food.ordering.system.kafka.producer.service.KafkaProducer;
import net.ontopsolutions.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import net.ontopsolutions.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PayOrderKafkaMessagePublisher implements OrderPaidRestaurantRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer;
    private final OrderKafkaMessageHelper orderKafkaMessageHelper;

    public PayOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                         OrderServiceConfigData orderServiceConfigData,
                                         KafkaProducer<String, RestaurantApprovalRequestAvroModel> kafkaProducer,
                                         OrderKafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    @Override
    public void publish(OrderPaidEvent event) {
        String orderId = event.getOrder().getId().getValue().toString();
        log.info("Received OrderPaidEvent for order id {}", orderId);

        try {
            RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel = orderMessagingDataMapper
                    .mapOrderPaidEventAsRestaurantApprovalRequestAvroModel(event);
            kafkaProducer.send(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                    orderId, restaurantApprovalRequestAvroModel,
                    orderKafkaMessageHelper.getKafkaCallback(orderServiceConfigData.getRestaurantApprovalRequestTopicName(),
                            restaurantApprovalRequestAvroModel,
                            orderId, "RestaurantApprovalRequestAvroModel"));
            log.info("RestaurantApprovalRequestAvroModel sent to Kafka for order id {} ", restaurantApprovalRequestAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalRequestAvroModel for order id: {} error: {}",
                    orderId, e.getMessage());
        }

    }
}
