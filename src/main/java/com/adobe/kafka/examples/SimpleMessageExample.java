package com.adobe.kafka.examples;

import com.adobe.kafka.handler.MessageHandler;
import com.adobe.kafka.publisher.KafkaMessagePublisher;
import com.adobe.kafka.subscriber.KafkaMessageSubscriber;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Example demonstrating basic usage of the Kafka Worker library.
 * This example shows how to publish and subscribe to messages using simple handlers.
 */
@SpringBootApplication
public class SimpleMessageExample implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleMessageExample.class);
    
    private final KafkaMessagePublisher publisher;
    private final KafkaMessageSubscriber subscriber;
    
    public SimpleMessageExample(KafkaMessagePublisher publisher, KafkaMessageSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleMessageExample.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Example 1: Simple message handler
        MessageHandler<String, String> simpleHandler = new MessageHandler<String, String>() {
            @Override
            public void handle(ConsumerRecord<String, String> record) {
                logger.info("Received message: key={}, value={}, topic={}, partition={}, offset={}", 
                        record.key(), record.value(), record.topic(), record.partition(), record.offset());
            }
        };
        
        // Subscribe to a topic
        String subscriptionId = subscriber.subscribe("simple-topic", simpleHandler);
        logger.info("Subscribed to simple-topic with subscription ID: {}", subscriptionId);
        
        // Publish some messages
        publisher.sendAsync("simple-topic", "Hello, Kafka!")
                .thenAccept(result -> logger.info("Message sent successfully: {}", result.getRecordMetadata()))
                .exceptionally(throwable -> {
                    logger.error("Failed to send message", throwable);
                    return null;
                });
        
        publisher.sendAsync("simple-topic", "user-123", "User logged in")
                .thenAccept(result -> logger.info("Keyed message sent successfully: {}", result.getRecordMetadata()))
                .exceptionally(throwable -> {
                    logger.error("Failed to send keyed message", throwable);
                    return null;
                });
        
        // Keep the application running for a while to see the messages
        Thread.sleep(5000);
        
        // Unsubscribe
        subscriber.unsubscribe(subscriptionId);
        logger.info("Unsubscribed from simple-topic");
    }
}
