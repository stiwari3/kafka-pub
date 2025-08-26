package com.adobe.kafka.handler;

import com.adobe.kafka.logging.MessageLogger;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Message handler interface with built-in error handling capabilities.
 * This interface allows teams to define both success and error handling logic.
 * 
 * @param <K> the type of the message key
 * @param <V> the type of the message value
 */
public interface ErrorAwareMessageHandler<K, V> {
    
    /**
     * Process a single Kafka message.
     * 
     * @param record the Kafka consumer record containing the message
     * @throws Exception if message processing fails
     */
    void handle(ConsumerRecord<K, V> record) throws Exception;
    
    /**
     * Handle errors that occur during message processing.
     * This method is called when the handle method throws an exception.
     * 
     * @param record the Kafka consumer record that failed to process
     * @param exception the exception that occurred during processing
     */
    default void handleError(ConsumerRecord<K, V> record, Exception exception) {
        // Default implementation uses centralized logging with MDC context
        MessageLogger.logTrackableException(exception, "ErrorAwareMessageHandler error handling");
    }
    
    /**
     * Determine whether to retry processing after an error.
     * 
     * @param record the Kafka consumer record that failed to process
     * @param exception the exception that occurred during processing
     * @param attemptCount the number of attempts made so far (starting from 1)
     * @return true if the message should be retried, false otherwise
     */
    default boolean shouldRetry(ConsumerRecord<K, V> record, Exception exception, int attemptCount) {
        return attemptCount < 3; // Default: retry up to 3 times
    }
}
