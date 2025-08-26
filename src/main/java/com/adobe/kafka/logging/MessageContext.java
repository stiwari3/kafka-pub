package com.adobe.kafka.logging;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

/**
 * A managed context for Kafka message processing that automatically handles MDC lifecycle.
 * This class implements AutoCloseable to ensure proper cleanup of MDC context.
 */
public class MessageContext implements AutoCloseable {
    
    private final String messageId;
    private final long startTime;
    private final boolean isBatch;
    
    /**
     * Creates a message context for single message processing.
     * 
     * @param record the Kafka consumer record
     * @param subscriptionId the subscription ID processing this message
     * @param handlerType the type of handler processing this message
     */
    public MessageContext(ConsumerRecord<?, ?> record, String subscriptionId, String handlerType) {
        this.messageId = MessageLogger.setupMessageContext(record, subscriptionId, handlerType);
        this.startTime = System.currentTimeMillis();
        this.isBatch = false;
        
        MessageLogger.logMessageStart(record);
    }
    
    /**
     * Creates a message context for batch message processing.
     * 
     * @param records the list of Kafka consumer records
     * @param subscriptionId the subscription ID processing this batch
     * @param handlerType the type of handler processing this batch
     */
    public MessageContext(List<ConsumerRecord<?, ?>> records, String subscriptionId, String handlerType) {
        this.messageId = MessageLogger.setupBatchContext(records.size(), subscriptionId, handlerType);
        this.startTime = System.currentTimeMillis();
        this.isBatch = true;
        
        MessageLogger.logBatchStart(records.size());
    }
    
    /**
     * Gets the message ID for this context.
     * 
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Gets the processing start time.
     * 
     * @return start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the elapsed processing time.
     * 
     * @return elapsed time in milliseconds
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Checks if this context is for batch processing.
     * 
     * @return true if this is a batch context
     */
    public boolean isBatch() {
        return isBatch;
    }
    
    /**
     * Logs successful completion of processing.
     * This should be called when message/batch processing completes successfully.
     */
    public void logSuccess() {
        long processingTime = getElapsedTime();
        
        if (isBatch) {
            // For batch, we need to get the batch size from MDC
            String batchSizeStr = org.slf4j.MDC.get("batchSize");
            int batchSize = batchSizeStr != null ? Integer.parseInt(batchSizeStr) : 0;
            MessageLogger.logBatchSuccess(batchSize, processingTime);
        } else {
            // For single message, we need to reconstruct the record info from MDC
            String topic = MessageLogger.getCurrentTopic();
            String partition = org.slf4j.MDC.get(MessageLogger.PARTITION_KEY);
            String offset = org.slf4j.MDC.get(MessageLogger.OFFSET_KEY);
            String messageKey = org.slf4j.MDC.get(MessageLogger.MESSAGE_KEY_KEY);
            
            // Create a simple record representation for logging
            MessageLogger.logMessageSuccess(createDummyRecord(topic, partition, offset, messageKey), processingTime);
        }
    }
    
    /**
     * Logs a retry attempt.
     * 
     * @param attemptCount the current attempt number
     * @param maxAttempts the maximum number of attempts
     * @param exception the exception that caused the retry
     */
    public void logRetry(int attemptCount, int maxAttempts, Exception exception) {
        if (!isBatch) {
            String topic = MessageLogger.getCurrentTopic();
            String partition = org.slf4j.MDC.get(MessageLogger.PARTITION_KEY);
            String offset = org.slf4j.MDC.get(MessageLogger.OFFSET_KEY);
            String messageKey = org.slf4j.MDC.get(MessageLogger.MESSAGE_KEY_KEY);
            
            MessageLogger.logRetryAttempt(createDummyRecord(topic, partition, offset, messageKey), 
                    attemptCount, maxAttempts, exception);
        }
    }
    
    /**
     * Logs that processing has been abandoned.
     * 
     * @param finalAttemptCount the final attempt count
     * @param exception the final exception
     */
    public void logAbandoned(int finalAttemptCount, Exception exception) {
        if (!isBatch) {
            String topic = MessageLogger.getCurrentTopic();
            String partition = org.slf4j.MDC.get(MessageLogger.PARTITION_KEY);
            String offset = org.slf4j.MDC.get(MessageLogger.OFFSET_KEY);
            String messageKey = org.slf4j.MDC.get(MessageLogger.MESSAGE_KEY_KEY);
            
            MessageLogger.logMessageAbandoned(createDummyRecord(topic, partition, offset, messageKey), 
                    finalAttemptCount, exception);
        }
    }
    
    /**
     * Throws a trackable exception with the current context.
     * 
     * @param message the exception message
     * @throws TrackableKafkaException the trackable exception
     */
    public void throwTrackableException(String message) throws TrackableKafkaException {
        throw new TrackableKafkaException(message);
    }
    
    /**
     * Throws a trackable exception with the current context and a cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     * @throws TrackableKafkaException the trackable exception
     */
    public void throwTrackableException(String message, Throwable cause) throws TrackableKafkaException {
        throw new TrackableKafkaException(message, cause);
    }
    
    /**
     * Throws a trackable exception with the current context from a cause.
     * 
     * @param cause the underlying cause
     * @throws TrackableKafkaException the trackable exception
     */
    public void throwTrackableException(Throwable cause) throws TrackableKafkaException {
        throw new TrackableKafkaException(cause);
    }
    
    /**
     * Automatically cleans up the MDC context when the context is closed.
     * This ensures that the MDC doesn't leak between message processing sessions.
     */
    @Override
    public void close() {
        MessageLogger.clearMessageContext();
    }
    
    /**
     * Creates a dummy ConsumerRecord for logging purposes when we only have MDC data.
     * This is a workaround since we can't store the original record in all cases.
     */
    private ConsumerRecord<Object, Object> createDummyRecord(String topic, String partition, String offset, String messageKey) {
        return new ConsumerRecord<>(
                topic != null ? topic : "unknown",
                partition != null ? Integer.parseInt(partition) : -1,
                offset != null ? Long.parseLong(offset) : -1L,
                messageKey != null && !"null".equals(messageKey) ? messageKey : null,
                null // We don't have the value for logging, which is okay
        );
    }
}
