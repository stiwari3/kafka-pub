package com.adobe.kafka.logging;

/**
 * Utility class that provides convenient methods for handlers to throw trackable exceptions.
 * This class serves as the primary interface for extending workers/handlers to integrate
 * with the centralized logging system.
 */
public class ExceptionHelper {
    
    /**
     * Throws a trackable exception with the specified message.
     * The exception will automatically capture the current MDC context for traceability.
     * 
     * @param message the exception message
     * @throws TrackableKafkaException the trackable exception with current context
     */
    public static void throwTrackable(String message) throws TrackableKafkaException {
        throw new TrackableKafkaException(message);
    }
    
    /**
     * Throws a trackable exception with the specified message and cause.
     * The exception will automatically capture the current MDC context for traceability.
     * 
     * @param message the exception message
     * @param cause the underlying cause of the exception
     * @throws TrackableKafkaException the trackable exception with current context
     */
    public static void throwTrackable(String message, Throwable cause) throws TrackableKafkaException {
        throw new TrackableKafkaException(message, cause);
    }
    
    /**
     * Throws a trackable exception wrapping the specified cause.
     * The exception will automatically capture the current MDC context for traceability.
     * 
     * @param cause the underlying cause of the exception
     * @throws TrackableKafkaException the trackable exception with current context
     */
    public static void throwTrackable(Throwable cause) throws TrackableKafkaException {
        throw new TrackableKafkaException(cause);
    }
    
    /**
     * Throws a trackable exception for validation failures.
     * 
     * @param fieldName the name of the field that failed validation
     * @param value the invalid value
     * @param reason the reason for validation failure
     * @throws TrackableKafkaException the trackable exception with validation context
     */
    public static void throwValidationError(String fieldName, Object value, String reason) throws TrackableKafkaException {
        String message = String.format("Validation failed for field '%s' with value '%s': %s", 
                fieldName, value, reason);
        throw new TrackableKafkaException(message);
    }
    
    /**
     * Throws a trackable exception for business logic violations.
     * 
     * @param businessRule the business rule that was violated
     * @param context additional context about the violation
     * @throws TrackableKafkaException the trackable exception with business context
     */
    public static void throwBusinessRuleViolation(String businessRule, String context) throws TrackableKafkaException {
        String message = String.format("Business rule violation: %s - Context: %s", 
                businessRule, context);
        throw new TrackableKafkaException(message);
    }
    
    /**
     * Throws a trackable exception for data processing errors.
     * 
     * @param operation the operation that failed
     * @param data description of the data being processed
     * @param cause the underlying cause
     * @throws TrackableKafkaException the trackable exception with data processing context
     */
    public static void throwDataProcessingError(String operation, String data, Throwable cause) throws TrackableKafkaException {
        String message = String.format("Data processing error during '%s' for data: %s", 
                operation, data);
        throw new TrackableKafkaException(message, cause);
    }
    
    /**
     * Throws a trackable exception for external service integration errors.
     * 
     * @param serviceName the name of the external service
     * @param operation the operation that failed
     * @param cause the underlying cause
     * @throws TrackableKafkaException the trackable exception with service integration context
     */
    public static void throwServiceIntegrationError(String serviceName, String operation, Throwable cause) throws TrackableKafkaException {
        String message = String.format("External service integration error - Service: %s, Operation: %s", 
                serviceName, operation);
        throw new TrackableKafkaException(message, cause);
    }
    
    /**
     * Throws a trackable exception for retry exhaustion scenarios.
     * 
     * @param operation the operation that was retried
     * @param attempts the number of attempts made
     * @param lastError the last error encountered
     * @throws TrackableKafkaException the trackable exception with retry context
     */
    public static void throwRetryExhausted(String operation, int attempts, Throwable lastError) throws TrackableKafkaException {
        String message = String.format("Retry exhausted for operation '%s' after %d attempts", 
                operation, attempts);
        throw new TrackableKafkaException(message, lastError);
    }
    
    /**
     * Gets the current message ID from the MDC context.
     * This can be useful for handlers that need to include the message ID in their processing.
     * 
     * @return the current message ID or null if not available
     */
    public static String getCurrentMessageId() {
        return MessageLogger.getCurrentMessageId();
    }
    
    /**
     * Gets the current topic from the MDC context.
     * This can be useful for handlers that need to include the topic in their processing.
     * 
     * @return the current topic or null if not available
     */
    public static String getCurrentTopic() {
        return MessageLogger.getCurrentTopic();
    }
    
    /**
     * Logs a warning with the current message context.
     * This is useful for handlers that want to log warnings while maintaining traceability.
     * 
     * @param message the warning message
     */
    public static void logWarning(String message) {
        org.slf4j.LoggerFactory.getLogger(ExceptionHelper.class).warn(message);
    }
    
    /**
     * Logs an info message with the current message context.
     * This is useful for handlers that want to log information while maintaining traceability.
     * 
     * @param message the info message
     */
    public static void logInfo(String message) {
        org.slf4j.LoggerFactory.getLogger(ExceptionHelper.class).info(message);
    }
    
    /**
     * Logs a debug message with the current message context.
     * This is useful for handlers that want to log debug information while maintaining traceability.
     * 
     * @param message the debug message
     */
    public static void logDebug(String message) {
        org.slf4j.LoggerFactory.getLogger(ExceptionHelper.class).debug(message);
    }
    
    /**
     * Gets the current partition from the MDC context.
     * This can be useful for handlers that need to include the partition in their processing.
     * 
     * @return the current partition or null if not available
     */
    public static String getCurrentPartition() {
        return org.slf4j.MDC.get(MessageLogger.PARTITION_KEY);
    }
    
    /**
     * Gets the current offset from the MDC context.
     * This can be useful for handlers that need to include the offset in their processing.
     * 
     * @return the current offset or null if not available
     */
    public static String getCurrentOffset() {
        return org.slf4j.MDC.get(MessageLogger.OFFSET_KEY);
    }
    
    /**
     * Gets the current message key from the MDC context.
     * This can be useful for handlers that need to include the message key in their processing.
     * 
     * @return the current message key or null if not available
     */
    public static String getCurrentMessageKey() {
        return org.slf4j.MDC.get(MessageLogger.MESSAGE_KEY_KEY);
    }
    
    /**
     * Gets the current subscription ID from the MDC context.
     * This can be useful for handlers that need to include the subscription ID in their processing.
     * 
     * @return the current subscription ID or null if not available
     */
    public static String getCurrentSubscriptionId() {
        return org.slf4j.MDC.get(MessageLogger.SUBSCRIPTION_ID_KEY);
    }
    
    /**
     * Gets the current handler type from the MDC context.
     * This can be useful for handlers that need to know what type of handler they are.
     * 
     * @return the current handler type or null if not available
     */
    public static String getCurrentHandlerType() {
        return org.slf4j.MDC.get(MessageLogger.HANDLER_TYPE_KEY);
    }
    
    /**
     * Gets all current MDC context as a formatted string.
     * This is useful for including full context information in logs or external API calls.
     * 
     * @return formatted string containing all MDC context
     */
    public static String getCurrentContextSummary() {
        String messageId = getCurrentMessageId();
        String topic = getCurrentTopic();
        String partition = getCurrentPartition();
        String offset = getCurrentOffset();
        String messageKey = getCurrentMessageKey();
        String subscriptionId = getCurrentSubscriptionId();
        String handlerType = getCurrentHandlerType();
        
        StringBuilder sb = new StringBuilder();
        sb.append("MessageContext[");
        
        if (messageId != null) sb.append("id=").append(messageId);
        if (topic != null) sb.append(", topic=").append(topic);
        if (partition != null) sb.append(", partition=").append(partition);
        if (offset != null) sb.append(", offset=").append(offset);
        if (messageKey != null && !"null".equals(messageKey)) sb.append(", key=").append(messageKey);
        if (subscriptionId != null) sb.append(", subscription=").append(subscriptionId);
        if (handlerType != null) sb.append(", handler=").append(handlerType);
        
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Checks if we are currently within a message processing context.
     * This can be useful for conditional logic based on whether MDC context is available.
     * 
     * @return true if currently within a message processing context
     */
    public static boolean isInMessageContext() {
        return getCurrentMessageId() != null;
    }
    
    /**
     * Executes a runnable with additional MDC context, preserving existing context.
     * This is useful when you want to add additional context for a specific operation.
     * 
     * @param key the MDC key to add
     * @param value the MDC value to add
     * @param runnable the operation to execute
     */
    public static void withAdditionalContext(String key, String value, Runnable runnable) {
        String originalValue = org.slf4j.MDC.get(key);
        try {
            org.slf4j.MDC.put(key, value);
            runnable.run();
        } finally {
            if (originalValue != null) {
                org.slf4j.MDC.put(key, originalValue);
            } else {
                org.slf4j.MDC.remove(key);
            }
        }
    }
    
    /**
     * Logs a message with additional temporary MDC context.
     * The additional context is only available for this single log entry.
     * 
     * @param level the log level (INFO, WARN, ERROR, DEBUG)
     * @param message the log message
     * @param contextKey additional context key
     * @param contextValue additional context value
     */
    public static void logWithAdditionalContext(String level, String message, String contextKey, String contextValue) {
        withAdditionalContext(contextKey, contextValue, () -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ExceptionHelper.class);
            switch (level.toUpperCase()) {
                case "INFO" -> logger.info(message);
                case "WARN" -> logger.warn(message);
                case "ERROR" -> logger.error(message);
                case "DEBUG" -> logger.debug(message);
                default -> logger.info(message);
            }
        });
    }
}
