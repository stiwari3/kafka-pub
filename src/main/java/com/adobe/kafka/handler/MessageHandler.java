package com.adobe.kafka.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Basic message handler interface for processing Kafka messages.
 * Teams can implement this interface to define custom message processing logic.
 * 
 * @param <K> the type of the message key
 * @param <V> the type of the message value
 */
@FunctionalInterface
public interface MessageHandler<K, V> {
    
    /**
     * Process a single Kafka message.
     * 
     * @param record the Kafka consumer record containing the message
     * @throws Exception if message processing fails
     */
    void handle(ConsumerRecord<K, V> record) throws Exception;
}
