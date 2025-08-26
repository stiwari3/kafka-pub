package com.adobe.kafka.subscriber;

import com.adobe.kafka.handler.BatchMessageHandler;
import com.adobe.kafka.handler.ErrorAwareMessageHandler;
import com.adobe.kafka.handler.MessageHandler;
import com.adobe.kafka.handler.TypedMessageHandler;

import java.util.List;

/**
 * Interface for subscribing to Kafka topics and registering message handlers.
 * Provides various ways to handle incoming messages from Kafka topics.
 */
public interface KafkaMessageSubscriber {
    
    /**
     * Subscribe to a topic with a basic message handler.
     * 
     * @param topic the topic to subscribe to
     * @param handler the message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(String topic, MessageHandler<K, V> handler);
    
    /**
     * Subscribe to multiple topics with a basic message handler.
     * 
     * @param topics the topics to subscribe to
     * @param handler the message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(List<String> topics, MessageHandler<K, V> handler);
    
    /**
     * Subscribe to a topic with a typed message handler.
     * 
     * @param topic the topic to subscribe to
     * @param handler the typed message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(String topic, TypedMessageHandler<K, V> handler);
    
    /**
     * Subscribe to multiple topics with a typed message handler.
     * 
     * @param topics the topics to subscribe to
     * @param handler the typed message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(List<String> topics, TypedMessageHandler<K, V> handler);
    
    /**
     * Subscribe to a topic with an error-aware message handler.
     * 
     * @param topic the topic to subscribe to
     * @param handler the error-aware message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(String topic, ErrorAwareMessageHandler<K, V> handler);
    
    /**
     * Subscribe to multiple topics with an error-aware message handler.
     * 
     * @param topics the topics to subscribe to
     * @param handler the error-aware message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(List<String> topics, ErrorAwareMessageHandler<K, V> handler);
    
    /**
     * Subscribe to a topic with a batch message handler.
     * 
     * @param topic the topic to subscribe to
     * @param handler the batch message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribeBatch(String topic, BatchMessageHandler<K, V> handler);
    
    /**
     * Subscribe to multiple topics with a batch message handler.
     * 
     * @param topics the topics to subscribe to
     * @param handler the batch message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribeBatch(List<String> topics, BatchMessageHandler<K, V> handler);
    
    /**
     * Subscribe to a topic with a specific consumer group.
     * 
     * @param topic the topic to subscribe to
     * @param consumerGroup the consumer group id
     * @param handler the message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(String topic, String consumerGroup, MessageHandler<K, V> handler);
    
    /**
     * Subscribe to multiple topics with a specific consumer group.
     * 
     * @param topics the topics to subscribe to
     * @param consumerGroup the consumer group id
     * @param handler the message handler
     * @param <K> the type of the message key
     * @param <V> the type of the message value
     * @return a subscription identifier that can be used to manage the subscription
     */
    <K, V> String subscribe(List<String> topics, String consumerGroup, MessageHandler<K, V> handler);
    
    /**
     * Unsubscribe from a topic subscription.
     * 
     * @param subscriptionId the subscription identifier returned from subscribe methods
     */
    void unsubscribe(String subscriptionId);
    
    /**
     * Pause consumption for a subscription.
     * 
     * @param subscriptionId the subscription identifier
     */
    void pause(String subscriptionId);
    
    /**
     * Resume consumption for a paused subscription.
     * 
     * @param subscriptionId the subscription identifier
     */
    void resume(String subscriptionId);
    
    /**
     * Check if a subscription is active.
     * 
     * @param subscriptionId the subscription identifier
     * @return true if the subscription is active, false otherwise
     */
    boolean isActive(String subscriptionId);
    
    /**
     * Get all active subscription identifiers.
     * 
     * @return list of active subscription identifiers
     */
    List<String> getActiveSubscriptions();
}
