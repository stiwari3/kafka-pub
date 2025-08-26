package com.adobe.kafka.examples;

import com.adobe.kafka.handler.ErrorAwareMessageHandler;
import com.adobe.kafka.handler.TypedMessageHandler;
import com.adobe.kafka.publisher.KafkaMessagePublisher;
import com.adobe.kafka.subscriber.KafkaMessageSubscriber;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Advanced example demonstrating typed message handling and error handling capabilities.
 */
@SpringBootApplication
public class AdvancedMessageExample implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedMessageExample.class);
    
    private final KafkaMessagePublisher publisher;
    private final KafkaMessageSubscriber subscriber;
    
    public AdvancedMessageExample(KafkaMessagePublisher publisher, KafkaMessageSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(AdvancedMessageExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        
        // Example 1: Typed message handler
        TypedMessageHandler<String, UserEvent> typedHandler = (key, value, topic, partition, offset) -> {
            logger.info("Processing user event: userId={}, action={}, timestamp={} from topic={}", 
                    value.getUserId(), value.getAction(), value.getTimestamp(), topic);
            
            // Process the user event
            processUserEvent(value);
        };
        
        String typedSubscriptionId = subscriber.subscribe("user-events", typedHandler);
        logger.info("Subscribed to user-events with typed handler");
        
        // Example 2: Error-aware message handler
        ErrorAwareMessageHandler<String, OrderEvent> errorAwareHandler = new ErrorAwareMessageHandler<String, OrderEvent>() {
            @Override
            public void handle(ConsumerRecord<String, OrderEvent> record) throws Exception {
                OrderEvent order = record.value();
                logger.info("Processing order: orderId={}, amount={}", order.getOrderId(), order.getAmount());
                
                // Simulate processing that might fail
                if (order.getAmount() < 0) {
                    throw new IllegalArgumentException("Order amount cannot be negative");
                }
                
                processOrder(order);
                logger.info("Order processed successfully: {}", order.getOrderId());
            }
            
            @Override
            public void handleError(ConsumerRecord<String, OrderEvent> record, Exception exception) {
                OrderEvent order = record.value();
                logger.error("Failed to process order {}: {}", order.getOrderId(), exception.getMessage());
                
                // Send to dead letter queue or error topic
                sendToErrorTopic(record, exception);
            }
            
            @Override
            public boolean shouldRetry(ConsumerRecord<String, OrderEvent> record, Exception exception, int attemptCount) {
                // Retry for transient errors, but not for validation errors
                if (exception instanceof IllegalArgumentException) {
                    return false; // Don't retry validation errors
                }
                return attemptCount < 3; // Retry up to 3 times for other errors
            }
        };
        
        String errorAwareSubscriptionId = subscriber.subscribe("order-events", errorAwareHandler);
        logger.info("Subscribed to order-events with error-aware handler");
        
        // Publish some test messages
        publishTestMessages();
        
        // Keep the application running
        Thread.sleep(10000);
        
        // Cleanup
        subscriber.unsubscribe(typedSubscriptionId);
        subscriber.unsubscribe(errorAwareSubscriptionId);
        logger.info("Unsubscribed from all topics");
    }
    
    private void publishTestMessages() throws Exception {
        // Publish user events
        UserEvent userEvent1 = new UserEvent("user-123", "LOGIN", LocalDateTime.now());
        UserEvent userEvent2 = new UserEvent("user-456", "PURCHASE", LocalDateTime.now());
        
        Map<String, Object> headers = new HashMap<>();
        headers.put("source", "user-service");
        headers.put("version", "1.0");
        
        publisher.sendAsync("user-events", userEvent1.getUserId(), userEvent1, headers);
        publisher.sendAsync("user-events", userEvent2.getUserId(), userEvent2, headers);
        
        // Publish order events (including one that will fail)
        OrderEvent order1 = new OrderEvent("order-123", 99.99);
        OrderEvent order2 = new OrderEvent("order-456", -10.00); // This will fail
        OrderEvent order3 = new OrderEvent("order-789", 149.99);
        
        publisher.sendAsync("order-events", order1.getOrderId(), order1);
        publisher.sendAsync("order-events", order2.getOrderId(), order2);
        publisher.sendAsync("order-events", order3.getOrderId(), order3);
        
        logger.info("Published test messages");
    }
    
    private void processUserEvent(UserEvent event) {
        // Simulate user event processing
        logger.info("User event processed: {} performed {}", event.getUserId(), event.getAction());
    }
    
    private void processOrder(OrderEvent order) {
        // Simulate order processing
        logger.info("Order processed: {} for amount ${}", order.getOrderId(), order.getAmount());
    }
    
    private void sendToErrorTopic(ConsumerRecord<String, OrderEvent> record, Exception exception) {
        try {
            Map<String, Object> errorHeaders = new HashMap<>();
            errorHeaders.put("error-message", exception.getMessage());
            errorHeaders.put("original-topic", record.topic());
            errorHeaders.put("original-partition", String.valueOf(record.partition()));
            errorHeaders.put("original-offset", String.valueOf(record.offset()));
            
            publisher.sendAsync("order-events-error", record.key(), record.value(), errorHeaders);
            logger.info("Sent failed message to error topic");
        } catch (Exception e) {
            logger.error("Failed to send message to error topic", e);
        }
    }
    
    // Example domain objects
    public static class UserEvent {
        @JsonProperty("userId")
        private String userId;
        
        @JsonProperty("action")
        private String action;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        public UserEvent() {}
        
        public UserEvent(String userId, String action, LocalDateTime timestamp) {
            this.userId = userId;
            this.action = action;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class OrderEvent {
        @JsonProperty("orderId")
        private String orderId;
        
        @JsonProperty("amount")
        private double amount;
        
        public OrderEvent() {}
        
        public OrderEvent(String orderId, double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }
        
        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}
