package com.food.ordering.system.kafka.producer.service;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.support.SendResult;

import org.apache.avro.specific.SpecificRecordBase;

public interface KafkaProducer<K extends Serializable, V extends SpecificRecordBase> {
    CompletableFuture<SendResult<K, V>> send(String topicName, K key, V message);
}
