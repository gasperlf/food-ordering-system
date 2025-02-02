package net.ontopsolutions.food.ordering.system.order.service.messaging.publisher.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class OrderKafkaMessageHelper {

    public <T> ListenableFutureCallback<SendResult<String, T>> getKafkaCallback(String requestTopicName, T requestAvroModel,
                                                                                String orderId, String requestAvroModelName) {
        return new ListenableFutureCallback<SendResult<String, T>>() {
            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while sending {} message {} to topic {}",requestAvroModelName, requestAvroModel.toString(),
                        requestTopicName);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("Received successful response from Kafka for order id {} Topic {}  Partition {} Offset {} Timestamp {}",
                        orderId,
                        recordMetadata.topic(),
                        recordMetadata.partition(),
                        recordMetadata.offset(),
                        recordMetadata.timestamp()
                );
            }
        };
    }
}
