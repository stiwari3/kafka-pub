package com.adobe.kafka.logging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Centralized logging utility for Kafka message processing with MDC support.
 * This class provides trackable logging capabilities for messages processed across the system.
 */
public class MessageLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageLogger.class);
    
    // MDC Keys
    public static final String MESSAGE_ID_KEY = "messageId";
    public static final String TOPIC_KEY = "topic";
    public static final String PARTITION_KEY = "partition";
    public static final String OFFSET_KEY = "offset";
    public static final String MESSAGE_KEY_KEY = "messageKey";
    public static final String SUBSCRIPTION_ID_KEY = "subscriptionId";
    public static final String HANDLER_TYPE_KEY = "handlerType";
    
    /**
     * Sets up MDC context for a Kafka message processing session.
     * 
     * @param record the Kafka consumer record
     * @param subscriptionId the subscription ID processing this message
     * @param handlerType the type of handler processing this message
     * @return the generated message ID for tracking
     */
    public static String setupMessageContext(ConsumerRecord<?, ?> record, String subscriptionId, String handlerType) {
        String messageId = generateMessageId();
        
        MDC.put(MESSAGE_ID_KEY, messageId);
        MDC.put(TOPIC_KEY, record.topic());
        MDC.put(PARTITION_KEY, String.valueOf(record.partition()));
        MDC.put(OFFSET_KEY, String.valueOf(record.offset()));
        MDC.put(MESSAGE_KEY_KEY, record.key() != null ? record.key().toString() : "null");
        MDC.put(SUBSCRIPTION_ID_KEY, subscriptionId);
        MDC.put(HANDLER_TYPE_KEY, handlerType);
        
        return messageId;
    }
    
    /**
     * Sets up MDC context for batch message processing.
     * 
     * @param batchSize the number of messages in the batch
     * @param subscriptionId the subscription ID processing this batch
     * @param handlerType the type of handler processing this batch
     * @return the generated batch ID for tracking
     */
    public static String setupBatchContext(int batchSize, String subscriptionId, String handlerType) {
        String batchId = generateBatchId();
        
        MDC.put(MESSAGE_ID_KEY, batchId);
        MDC.put("batchSize", String.valueOf(batchSize));
        MDC.put(SUBSCRIPTION_ID_KEY, subscriptionId);
        MDC.put(HANDLER_TYPE_KEY, handlerType);
        
        return batchId;
    }
    
    /**
     * Clears the MDC context after message processing.
     */
    public static void clearMessageContext() {
        MDC.clear();
    }
    
    /**
     * Logs the start of message processing.
     * 
     * @param record the Kafka consumer record
     */
    public static void logMessageStart(ConsumerRecord<?, ?> record) {
        logger.info("Starting message processing - Topic: {}, Partition: {}, Offset: {}, Key: {}", 
                record.topic(), record.partition(), record.offset(), 
                record.key() != null ? record.key().toString() : "null");
    }
    
    /**
     * Logs successful completion of message processing.
     * 
     * @param record the Kafka consumer record
     * @param processingTimeMs the time taken to process the message in milliseconds
     */
    public static void logMessageSuccess(ConsumerRecord<?, ?> record, long processingTimeMs) {
        logger.info("Message processed successfully in {}ms", processingTimeMs);
    }
    
    /**
     * Logs batch processing start.
     * 
     * @param batchSize the number of messages in the batch
     */
    public static void logBatchStart(int batchSize) {
        logger.info("Starting batch processing - Batch size: {}", batchSize);
    }
    
    /**
     * Logs successful completion of batch processing.
     * 
     * @param batchSize the number of messages in the batch
     * @param processingTimeMs the time taken to process the batch in milliseconds
     */
    public static void logBatchSuccess(int batchSize, long processingTimeMs) {
        logger.info("Batch processed successfully - {} messages in {}ms", batchSize, processingTimeMs);
    }
    
    /**
     * Logs message processing retry attempts.
     * 
     * @param record the Kafka consumer record
     * @param attemptCount the current attempt number
     * @param maxAttempts the maximum number of attempts
     * @param exception the exception that caused the retry
     */
    public static void logRetryAttempt(ConsumerRecord<?, ?> record, int attemptCount, int maxAttempts, Exception exception) {
        logger.warn("Retry attempt {}/{} for message processing - Error: {}", 
                attemptCount, maxAttempts, exception.getMessage());
    }
    
    /**
     * Logs when message processing is being abandoned after exhausting retries.
     * 
     * @param record the Kafka consumer record
     * @param finalAttemptCount the final attempt count
     * @param exception the final exception
     */
    public static void logMessageAbandoned(ConsumerRecord<?, ?> record, int finalAttemptCount, Exception exception) {
        logger.error("Message processing abandoned after {} attempts - Final error: {}", 
                finalAttemptCount, exception.getMessage(), exception);
    }
    
    /**
     * Logs a trackable exception with full context.
     * 
     * @param exception the exception to log
     * @param context additional context information
     */
    public static void logTrackableException(Exception exception, String context) {
        logger.error("Trackable exception occurred - Context: {} - Error: {}", 
                context, exception.getMessage(), exception);
    }
    
    /**
     * Logs a trackable exception without additional context.
     * 
     * @param exception the exception to log
     */
    public static void logTrackableException(Exception exception) {
        logTrackableException(exception, "Message processing");
    }
    
    /**
     * Gets the current message ID from MDC.
     * 
     * @return the current message ID or null if not set
     */
    public static String getCurrentMessageId() {
        return MDC.get(MESSAGE_ID_KEY);
    }
    
    /**
     * Gets the current topic from MDC.
     * 
     * @return the current topic or null if not set
     */
    public static String getCurrentTopic() {
        return MDC.get(TOPIC_KEY);
    }
    
    /**
     * Generates a unique message ID for tracking.
     * 
     * @return a unique message ID
     */
    private static String generateMessageId() {
        return "msg-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Generates a unique batch ID for tracking.
     * 
     * @return a unique batch ID
     */
    private static String generateBatchId() {
        return "batch-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
