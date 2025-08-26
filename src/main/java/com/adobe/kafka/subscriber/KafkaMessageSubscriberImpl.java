package com.adobe.kafka.subscriber;

import com.adobe.kafka.handler.BatchMessageHandler;
import com.adobe.kafka.handler.ErrorAwareMessageHandler;
import com.adobe.kafka.handler.MessageHandler;
import com.adobe.kafka.handler.TypedMessageHandler;
import com.adobe.kafka.logging.MessageContext;
import com.adobe.kafka.logging.MessageLogger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of KafkaMessageSubscriber using Spring Kafka's listener capabilities.
 * This implementation manages dynamic subscriptions and provides lifecycle management for consumers.
 */
@Component
public class KafkaMessageSubscriberImpl implements KafkaMessageSubscriber {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageSubscriberImpl.class);
    
    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, MessageListenerContainer> containers = new ConcurrentHashMap<>();
    
    public KafkaMessageSubscriberImpl(KafkaListenerEndpointRegistry endpointRegistry) {
        this.endpointRegistry = endpointRegistry;
    }
    
    @Override
    public <K, V> String subscribe(String topic, MessageHandler<K, V> handler) {
        return subscribe(List.of(topic), handler);
    }
    
    @Override
    public <K, V> String subscribe(List<String> topics, MessageHandler<K, V> handler) {
        String subscriptionId = generateSubscriptionId();
        logger.info("Creating subscription {} for topics: {}", subscriptionId, topics);
        
        // Wrap the MessageHandler with centralized logging
        MessageHandler<K, V> wrappedHandler = record -> {
            try (MessageContext context = new MessageContext(record, subscriptionId, "MessageHandler")) {
                handler.handle(record);
                context.logSuccess();
            }
        };
        
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, topics, wrappedHandler);
        subscriptions.put(subscriptionId, info);
        
        // For now, we'll use a placeholder for dynamic registration
        // In a full implementation, you would create and register a MessageListenerContainer here
        logger.info("Subscription {} created successfully", subscriptionId);
        
        return subscriptionId;
    }
    
    @Override
    public <K, V> String subscribe(String topic, TypedMessageHandler<K, V> handler) {
        return subscribe(List.of(topic), handler);
    }
    
    @Override
    public <K, V> String subscribe(List<String> topics, TypedMessageHandler<K, V> handler) {
        String subscriptionId = generateSubscriptionId();
        logger.info("Creating typed subscription {} for topics: {}", subscriptionId, topics);
        
        // Wrap the TypedMessageHandler in a MessageHandler with centralized logging
        MessageHandler<K, V> wrappedHandler = record -> {
            try (MessageContext context = new MessageContext(record, subscriptionId, "TypedMessageHandler")) {
                handler.handle(record.key(), record.value(), record.topic(), record.partition(), record.offset());
                context.logSuccess();
            }
        };
        
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, topics, wrappedHandler);
        subscriptions.put(subscriptionId, info);
        
        logger.info("Typed subscription {} created successfully", subscriptionId);
        
        return subscriptionId;
    }
    
    @Override
    public <K, V> String subscribe(String topic, ErrorAwareMessageHandler<K, V> handler) {
        return subscribe(List.of(topic), handler);
    }
    
    @Override
    public <K, V> String subscribe(List<String> topics, ErrorAwareMessageHandler<K, V> handler) {
        String subscriptionId = generateSubscriptionId();
        logger.info("Creating error-aware subscription {} for topics: {}", subscriptionId, topics);
        
        // Wrap the ErrorAwareMessageHandler in a MessageHandler with centralized error handling
        MessageHandler<K, V> wrappedHandler = record -> {
            try (MessageContext context = new MessageContext(record, subscriptionId, "ErrorAwareMessageHandler")) {
                int attemptCount = 0;
                boolean success = false;
                Exception lastException = null;
                
                while (!success && attemptCount < 5) { // Max 5 attempts
                    attemptCount++;
                    try {
                        handler.handle(record);
                        success = true;
                        context.logSuccess();
                    } catch (Exception e) {
                        lastException = e;
                        context.logRetry(attemptCount, 5, e);
                        
                        handler.handleError(record, e);
                        
                        if (!handler.shouldRetry(record, e, attemptCount)) {
                            context.logAbandoned(attemptCount, e);
                            break;
                        }
                        
                        // Simple backoff strategy
                        try {
                            Thread.sleep(1000 * attemptCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // If we failed all attempts, log it as a trackable exception
                if (!success && lastException != null) {
                    MessageLogger.logTrackableException(lastException, "Message processing failed after all retry attempts");
                }
            }
        };
        
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, topics, wrappedHandler);
        subscriptions.put(subscriptionId, info);
        
        logger.info("Error-aware subscription {} created successfully", subscriptionId);
        
        return subscriptionId;
    }
    
    @Override
    public <K, V> String subscribeBatch(String topic, BatchMessageHandler<K, V> handler) {
        return subscribeBatch(List.of(topic), handler);
    }
    
    @Override
    public <K, V> String subscribeBatch(List<String> topics, BatchMessageHandler<K, V> handler) {
        String subscriptionId = generateSubscriptionId();
        logger.info("Creating batch subscription {} for topics: {}", subscriptionId, topics);
        
        // Wrap the BatchMessageHandler with centralized logging
        BatchMessageHandler<K, V> wrappedHandler = records -> {
            // Convert to wildcard type for MessageContext constructor
            List<ConsumerRecord<?, ?>> wildcardRecords = new ArrayList<>(records);
            try (MessageContext context = new MessageContext(wildcardRecords, subscriptionId, "BatchMessageHandler")) {
                handler.handleBatch(records);
                context.logSuccess();
            }
        };
        
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, topics, wrappedHandler);
        subscriptions.put(subscriptionId, info);
        
        logger.info("Batch subscription {} created successfully", subscriptionId);
        
        return subscriptionId;
    }
    
    @Override
    public <K, V> String subscribe(String topic, String consumerGroup, MessageHandler<K, V> handler) {
        return subscribe(List.of(topic), consumerGroup, handler);
    }
    
    @Override
    public <K, V> String subscribe(List<String> topics, String consumerGroup, MessageHandler<K, V> handler) {
        String subscriptionId = generateSubscriptionId();
        logger.info("Creating subscription {} for topics: {} with consumer group: {}", subscriptionId, topics, consumerGroup);
        
        // Wrap the MessageHandler with centralized logging
        MessageHandler<K, V> wrappedHandler = record -> {
            try (MessageContext context = new MessageContext(record, subscriptionId, "MessageHandler[" + consumerGroup + "]")) {
                handler.handle(record);
                context.logSuccess();
            }
        };
        
        SubscriptionInfo info = new SubscriptionInfo(subscriptionId, topics, wrappedHandler, consumerGroup);
        subscriptions.put(subscriptionId, info);
        
        logger.info("Subscription {} with consumer group {} created successfully", subscriptionId, consumerGroup);
        
        return subscriptionId;
    }
    
    @Override
    public void unsubscribe(String subscriptionId) {
        logger.info("Unsubscribing from subscription: {}", subscriptionId);
        
        SubscriptionInfo info = subscriptions.remove(subscriptionId);
        if (info != null) {
            MessageListenerContainer container = containers.remove(subscriptionId);
            if (container != null) {
                container.stop();
            }
            logger.info("Successfully unsubscribed from subscription: {}", subscriptionId);
        } else {
            logger.warn("Subscription {} not found for unsubscribe", subscriptionId);
        }
    }
    
    @Override
    public void pause(String subscriptionId) {
        logger.info("Pausing subscription: {}", subscriptionId);
        
        SubscriptionInfo info = subscriptions.get(subscriptionId);
        if (info != null) {
            MessageListenerContainer container = containers.get(subscriptionId);
            if (container != null) {
                container.pause();
                info.setPaused(true);
                logger.info("Successfully paused subscription: {}", subscriptionId);
            }
        } else {
            logger.warn("Subscription {} not found for pause", subscriptionId);
        }
    }
    
    @Override
    public void resume(String subscriptionId) {
        logger.info("Resuming subscription: {}", subscriptionId);
        
        SubscriptionInfo info = subscriptions.get(subscriptionId);
        if (info != null) {
            MessageListenerContainer container = containers.get(subscriptionId);
            if (container != null) {
                container.resume();
                info.setPaused(false);
                logger.info("Successfully resumed subscription: {}", subscriptionId);
            }
        } else {
            logger.warn("Subscription {} not found for resume", subscriptionId);
        }
    }
    
    @Override
    public boolean isActive(String subscriptionId) {
        SubscriptionInfo info = subscriptions.get(subscriptionId);
        return info != null && !info.isPaused();
    }
    
    @Override
    public List<String> getActiveSubscriptions() {
        return subscriptions.keySet().stream()
                .filter(this::isActive)
                .toList();
    }
    
    private String generateSubscriptionId() {
        return "sub-" + UUID.randomUUID().toString();
    }
    
    /**
     * Internal class to hold subscription information.
     */
    private static class SubscriptionInfo {
        private final String subscriptionId;
        private final List<String> topics;
        private final Object handler;
        private final String consumerGroup;
        private boolean paused = false;
        
        public SubscriptionInfo(String subscriptionId, List<String> topics, Object handler) {
            this(subscriptionId, topics, handler, null);
        }
        
        public SubscriptionInfo(String subscriptionId, List<String> topics, Object handler, String consumerGroup) {
            this.subscriptionId = subscriptionId;
            this.topics = new ArrayList<>(topics);
            this.handler = handler;
            this.consumerGroup = consumerGroup;
        }
        
        public String getSubscriptionId() { return subscriptionId; }
        public List<String> getTopics() { return topics; }
        public Object getHandler() { return handler; }
        public String getConsumerGroup() { return consumerGroup; }
        public boolean isPaused() { return paused; }
        public void setPaused(boolean paused) { this.paused = paused; }
    }
}
