package com.adobe.kafka.logging;

import org.slf4j.MDC;

/**
 * A trackable exception that automatically captures and preserves MDC context
 * for better traceability across message processing flows.
 */
public class TrackableKafkaException extends Exception {
    
    private final String messageId;
    private final String topic;
    private final String partition;
    private final String offset;
    private final String subscriptionId;
    private final String handlerType;
    private final long timestamp;
    
    /**
     * Creates a trackable exception with the current MDC context.
     * 
     * @param message the exception message
     */
    public TrackableKafkaException(String message) {
        super(enrichMessage(message));
        this.messageId = MDC.get(MessageLogger.MESSAGE_ID_KEY);
        this.topic = MDC.get(MessageLogger.TOPIC_KEY);
        this.partition = MDC.get(MessageLogger.PARTITION_KEY);
        this.offset = MDC.get(MessageLogger.OFFSET_KEY);
        this.subscriptionId = MDC.get(MessageLogger.SUBSCRIPTION_ID_KEY);
        this.handlerType = MDC.get(MessageLogger.HANDLER_TYPE_KEY);
        this.timestamp = System.currentTimeMillis();
        
        // Log the exception immediately with full context
        MessageLogger.logTrackableException(this);
    }
    
    /**
     * Creates a trackable exception with the current MDC context and a cause.
     * 
     * @param message the exception message
     * @param cause the underlying cause
     */
    public TrackableKafkaException(String message, Throwable cause) {
        super(enrichMessage(message), cause);
        this.messageId = MDC.get(MessageLogger.MESSAGE_ID_KEY);
        this.topic = MDC.get(MessageLogger.TOPIC_KEY);
        this.partition = MDC.get(MessageLogger.PARTITION_KEY);
        this.offset = MDC.get(MessageLogger.OFFSET_KEY);
        this.subscriptionId = MDC.get(MessageLogger.SUBSCRIPTION_ID_KEY);
        this.handlerType = MDC.get(MessageLogger.HANDLER_TYPE_KEY);
        this.timestamp = System.currentTimeMillis();
        
        // Log the exception immediately with full context
        MessageLogger.logTrackableException(this);
    }
    
    /**
     * Creates a trackable exception with a cause and the current MDC context.
     * 
     * @param cause the underlying cause
     */
    public TrackableKafkaException(Throwable cause) {
        this("Exception occurred during message processing", cause);
    }
    
    /**
     * Gets the message ID associated with this exception.
     * 
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Gets the topic associated with this exception.
     * 
     * @return the topic name
     */
    public String getTopic() {
        return topic;
    }
    
    /**
     * Gets the partition associated with this exception.
     * 
     * @return the partition number as string
     */
    public String getPartition() {
        return partition;
    }
    
    /**
     * Gets the offset associated with this exception.
     * 
     * @return the offset as string
     */
    public String getOffset() {
        return offset;
    }
    
    /**
     * Gets the subscription ID associated with this exception.
     * 
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }
    
    /**
     * Gets the handler type associated with this exception.
     * 
     * @return the handler type
     */
    public String getHandlerType() {
        return handlerType;
    }
    
    /**
     * Gets the timestamp when this exception was created.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns a detailed string representation including all context information.
     * 
     * @return detailed exception information
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("TrackableKafkaException: ").append(getMessage()).append("\n");
        sb.append("Message ID: ").append(messageId).append("\n");
        sb.append("Topic: ").append(topic).append("\n");
        sb.append("Partition: ").append(partition).append("\n");
        sb.append("Offset: ").append(offset).append("\n");
        sb.append("Subscription ID: ").append(subscriptionId).append("\n");
        sb.append("Handler Type: ").append(handlerType).append("\n");
        sb.append("Timestamp: ").append(timestamp);
        
        if (getCause() != null) {
            sb.append("\nCaused by: ").append(getCause().toString());
        }
        
        return sb.toString();
    }
    
    /**
     * Checks if this exception has complete tracking context.
     * 
     * @return true if all tracking fields are present
     */
    public boolean hasCompleteContext() {
        return messageId != null && topic != null && partition != null && 
               offset != null && subscriptionId != null && handlerType != null;
    }
    
    /**
     * Enriches the exception message with context information if available.
     * 
     * @param originalMessage the original exception message
     * @return enriched message with context
     */
    private static String enrichMessage(String originalMessage) {
        String messageId = MDC.get(MessageLogger.MESSAGE_ID_KEY);
        String topic = MDC.get(MessageLogger.TOPIC_KEY);
        
        if (messageId != null || topic != null) {
            StringBuilder enriched = new StringBuilder();
            enriched.append(originalMessage);
            enriched.append(" [");
            
            if (messageId != null) {
                enriched.append("messageId=").append(messageId);
            }
            
            if (topic != null) {
                if (messageId != null) {
                    enriched.append(", ");
                }
                enriched.append("topic=").append(topic);
            }
            
            enriched.append("]");
            return enriched.toString();
        }
        
        return originalMessage;
    }
    
    @Override
    public String toString() {
        return getDetailedMessage();
    }
}
