package com.adobe.kafka.examples;

import com.adobe.kafka.handler.ErrorAwareMessageHandler;
import com.adobe.kafka.handler.MessageHandler;
import com.adobe.kafka.handler.TypedMessageHandler;
import com.adobe.kafka.logging.ExceptionHelper;
import com.adobe.kafka.logging.TrackableKafkaException;
import com.adobe.kafka.publisher.KafkaMessagePublisher;
import com.adobe.kafka.subscriber.KafkaMessageSubscriber;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example demonstrating the centralized logging capabilities of the Kafka Worker library.
 * This example shows how handlers can use ExceptionHelper to throw trackable exceptions
 * and how the centralized logging system maintains traceability across message processing.
 */
@SpringBootApplication
public class CentralizedLoggingExample implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CentralizedLoggingExample.class);
    
    private final KafkaMessagePublisher publisher;
    private final KafkaMessageSubscriber subscriber;
    
    public CentralizedLoggingExample(KafkaMessagePublisher publisher, KafkaMessageSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(CentralizedLoggingExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        
        // Example 1: MessageHandler with centralized logging
        MessageHandler<String, String> basicHandler = new MessageHandler<String, String>() {
            @Override
            public void handle(ConsumerRecord<String, String> record) throws Exception {
                // Log some information using the centralized system
                ExceptionHelper.logInfo("Processing message with value: " + record.value());
                
                // Simulate some business logic
                if (record.value().contains("error")) {
                    // Throw a trackable exception - this will be automatically logged with full context
                    ExceptionHelper.throwTrackable("Invalid message content detected");
                }
                
                if (record.value().contains("validate")) {
                    // Throw a validation error with specific details
                    ExceptionHelper.throwValidationError("message.content", record.value(), "contains invalid characters");
                }
                
                // Log successful processing
                ExceptionHelper.logInfo("Message processed successfully. MessageId: " + ExceptionHelper.getCurrentMessageId());
            }
        };
        
        // Example 2: TypedMessageHandler with business rule validation
        TypedMessageHandler<String, String> typedHandler = new TypedMessageHandler<String, String>() {
            @Override
            public void handle(String key, String value, String topic, int partition, long offset) throws Exception {
                ExceptionHelper.logInfo("Processing typed message - Key: " + key + ", Topic: " + topic);
                
                // Simulate business rule validation
                if (key != null && key.startsWith("invalid")) {
                    ExceptionHelper.throwBusinessRuleViolation("Key must not start with 'invalid'", "Key: " + key);
                }
                
                // Simulate external service call that might fail
                if (value.contains("service-error")) {
                    try {
                        // Simulate external service call
                        throw new RuntimeException("External service unavailable");
                    } catch (Exception e) {
                        ExceptionHelper.throwServiceIntegrationError("UserService", "validateUser", e);
                    }
                }
                
                ExceptionHelper.logInfo("Typed message processing completed");
            }
        };
        
        // Example 3: ErrorAwareMessageHandler with retry logic
        ErrorAwareMessageHandler<String, String> errorAwareHandler = new ErrorAwareMessageHandler<String, String>() {
            @Override
            public void handle(ConsumerRecord<String, String> record) throws Exception {
                ExceptionHelper.logInfo("Processing with error awareness - Value: " + record.value());
                
                // Simulate processing that might fail
                if (record.value().contains("transient-error")) {
                    // This will trigger retry logic in the subscriber
                    throw new RuntimeException("Transient processing error");
                }
                
                if (record.value().contains("fatal-error")) {
                    // This will be logged as a trackable exception
                    ExceptionHelper.throwTrackable("Fatal error occurred - cannot recover");
                }
                
                // Simulate data processing
                try {
                    processMessageData(record.value());
                } catch (Exception e) {
                    ExceptionHelper.throwDataProcessingError("messageTransformation", record.value(), e);
                }
                
                ExceptionHelper.logInfo("Error-aware processing completed successfully");
            }
            
            @Override
            public void handleError(ConsumerRecord<String, String> record, Exception exception) {
                // The default implementation now uses centralized logging
                // We can add custom error handling here if needed
                ExceptionHelper.logWarning("Custom error handling for message: " + record.value());
                
                // Call the default implementation to ensure centralized logging
                ErrorAwareMessageHandler.super.handleError(record, exception);
            }
            
            @Override
            public boolean shouldRetry(ConsumerRecord<String, String> record, Exception exception, int attemptCount) {
                // Retry transient errors, but not trackable exceptions or fatal errors
                if (exception instanceof TrackableKafkaException) {
                    return false; // Don't retry trackable exceptions
                }
                
                return attemptCount < 3; // Retry up to 3 times for other exceptions
            }
            
            private void processMessageData(String data) throws Exception {
                // Simulate data processing logic
                if (data.contains("invalid-json")) {
                    throw new IllegalArgumentException("Invalid JSON format");
                }
                // Process data...
            }
        };
        
        // Subscribe to different topics with different handlers
        String basicSubscription = subscriber.subscribe("basic-topic", basicHandler);
        String typedSubscription = subscriber.subscribe("typed-topic", typedHandler);
        String errorAwareSubscription = subscriber.subscribe("error-aware-topic", errorAwareHandler);
        
        logger.info("Created subscriptions: {}, {}, {}", basicSubscription, typedSubscription, errorAwareSubscription);
        
        // Publish test messages that will trigger different logging scenarios
        publisher.sendAsync("basic-topic", "normal-message")
                .thenAccept(result -> logger.info("Sent normal message"))
                .exceptionally(throwable -> {
                    logger.error("Failed to send normal message", throwable);
                    return null;
                });
        
        publisher.sendAsync("basic-topic", "message-with-error")
                .thenAccept(result -> logger.info("Sent error message"))
                .exceptionally(throwable -> {
                    logger.error("Failed to send error message", throwable);
                    return null;
                });
        
        publisher.sendAsync("typed-topic", "invalid-key", "business-rule-test")
                .thenAccept(result -> logger.info("Sent typed message"))
                .exceptionally(throwable -> {
                    logger.error("Failed to send typed message", throwable);
                    return null;
                });
        
        publisher.sendAsync("error-aware-topic", "transient-error-message")
                .thenAccept(result -> logger.info("Sent transient error message"))
                .exceptionally(throwable -> {
                    logger.error("Failed to send transient error message", throwable);
                    return null;
                });
        
        // Keep the application running to see the logging in action
        Thread.sleep(10000);
        
        // Clean up subscriptions
        subscriber.unsubscribe(basicSubscription);
        subscriber.unsubscribe(typedSubscription);
        subscriber.unsubscribe(errorAwareSubscription);
        
        logger.info("Centralized logging example completed");
    }
}
