package com.adobe.kafka.handler;

/**
 * Typed message handler interface for processing strongly-typed Kafka messages.
 * This interface provides a more convenient way to handle messages when you know
 * the exact types of keys and values.
 * 
 * @param <K> the type of the message key
 * @param <V> the type of the message value
 */
@FunctionalInterface
public interface TypedMessageHandler<K, V> {
    
    /**
     * Process a typed message with its key, value, topic, and partition information.
     * 
     * @param key the message key
     * @param value the message value
     * @param topic the topic name
     * @param partition the partition number
     * @param offset the message offset
     * @throws Exception if message processing fails
     */
    void handle(K key, V value, String topic, int partition, long offset) throws Exception;
}
