package com.food.ordering.system.order.service.messaging.listener.kafka;

/*
@Slf4j
@Component
public class CustomerKafkaListener implements KafkaConsumer<CustomerAvroModel> {

    private final CustomerMessageListener customerMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public CustomerKafkaListener(CustomerMessageListener customerMessageListener,
                                 OrderMessagingDataMapper orderMessagingDataMapper) {
        this.customerMessageListener = customerMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.customer-group-id}", topics = "${order-service.customer-topic-name}")
    public void receive(@Payload List<CustomerAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of customer create messages received with keys {}, partitions {} and offsets {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(customerAvroModel ->
                customerMessageListener.customerCreated(orderMessagingDataMapper
                        .customerAvroModeltoCustomerModel(customerAvroModel)));
    }
}
*/
