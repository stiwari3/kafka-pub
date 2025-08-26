package com.adobe.kafka.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import java.util.List;

/**
 * Batch message handler interface for processing multiple Kafka messages at once.
 * This is useful for scenarios where batch processing provides better performance
 * or when you need to process messages transactionally.
 * 
 * @param <K> the type of the message key
 * @param <V> the type of the message value
 */
@FunctionalInterface
public interface BatchMessageHandler<K, V> {
    
    /**
     * Process a batch of Kafka messages.
     * 
     * @param records list of Kafka consumer records to process
     * @throws Exception if batch processing fails
     */
    void handleBatch(List<ConsumerRecord<K, V>> records) throws Exception;
}
