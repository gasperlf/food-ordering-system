package net.ontopsolutions.food.ordering.system.order.service.messaging.listener.kafka;

import lombok.extern.slf4j.Slf4j;
import net.ontopsolutions.food.ordering.system.domain.dto.message.RestaurantApprovalResponse;
import net.ontopsolutions.food.ordering.system.domain.ports.input.message.listener.restaurantapproval.RestaurantApprovalMessageListener;
import net.ontopsolutions.food.ordering.system.kafka.consumer.KafkaConsumer;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import net.ontopsolutions.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import net.ontopsolutions.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseAvroModel> {

    private final RestaurantApprovalMessageListener restaurantApprovalMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantApprovalResponseKafkaListener(RestaurantApprovalMessageListener restaurantApprovalMessageListener,
                                                   OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantApprovalMessageListener = restaurantApprovalMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }


    @Override
    @KafkaListener(id = "${kafka-consumer-config.restaurant-approval-consumer-group-id}",
            topics = "${order-service.restaurant-approval-response-topic-name}")
    public void receive(@Payload List<RestaurantApprovalResponseAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {

        log.info("{} number of payment responses received with keys {}, partitions: {} and offsets: {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantApprovalRequestAvroModel -> {
            if(OrderApprovalStatus.APPROVED == restaurantApprovalRequestAvroModel.getOrderApprovalStatus()) {
                log.info("Processing approval order for order id: {}", restaurantApprovalRequestAvroModel.getOrderId());
                RestaurantApprovalResponse restaurantApprovalResponse = orderMessagingDataMapper
                        .mapRestaurantApprovalResponseAvroModelAsRestaurantApprovalResponse(restaurantApprovalRequestAvroModel);
                restaurantApprovalMessageListener.orderApproved(restaurantApprovalResponse);
            } else if (OrderApprovalStatus.REJECTED == restaurantApprovalRequestAvroModel.getOrderApprovalStatus()) {
                log.info("Processing reject order for order id: {}", restaurantApprovalRequestAvroModel.getOrderId());

                String.join(",", restaurantApprovalRequestAvroModel.getFailureMessages());
                RestaurantApprovalResponse restaurantApprovalResponse = orderMessagingDataMapper.mapRestaurantApprovalResponseAvroModelAsRestaurantApprovalResponse(restaurantApprovalRequestAvroModel);
                restaurantApprovalMessageListener.orderRejected(restaurantApprovalResponse);
            }
        });

    }
}
