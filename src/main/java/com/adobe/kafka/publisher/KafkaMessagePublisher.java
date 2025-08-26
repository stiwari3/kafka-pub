package com.adobe.kafka.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.Map;

/**
 * Interface for publishing messages to Kafka topics.
 * Provides both synchronous and asynchronous publishing capabilities.
 */
public interface KafkaMessagePublisher {
    
    /**
     * Send a message to a Kafka topic asynchronously.
     * 
     * @param topic the topic to send the message to
     * @param message the message to send
     * @param <T> the type of the message
     * @return a CompletableFuture containing the send result
     */
    <T> CompletableFuture<SendResult<String, T>> sendAsync(String topic, T message);
    
    /**
     * Send a message with a key to a Kafka topic asynchronously.
     * 
     * @param topic the topic to send the message to
     * @param key the message key
     * @param message the message to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return a CompletableFuture containing the send result
     */
    <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, K key, T message);
    
    /**
     * Send a message to a specific partition asynchronously.
     * 
     * @param topic the topic to send the message to
     * @param partition the partition to send to
     * @param key the message key
     * @param message the message to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return a CompletableFuture containing the send result
     */
    <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, Integer partition, K key, T message);
    
    /**
     * Send a message with headers asynchronously.
     * 
     * @param topic the topic to send the message to
     * @param key the message key
     * @param message the message to send
     * @param headers the message headers
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return a CompletableFuture containing the send result
     */
    <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, K key, T message, Map<String, Object> headers);
    
    /**
     * Send a ProducerRecord asynchronously.
     * 
     * @param record the producer record to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return a CompletableFuture containing the send result
     */
    <K, T> CompletableFuture<SendResult<K, T>> sendAsync(ProducerRecord<K, T> record);
    
    /**
     * Send a message to a Kafka topic synchronously.
     * 
     * @param topic the topic to send the message to
     * @param message the message to send
     * @param <T> the type of the message
     * @return the send result
     * @throws Exception if sending fails
     */
    <T> SendResult<String, T> send(String topic, T message) throws Exception;
    
    /**
     * Send a message with a key to a Kafka topic synchronously.
     * 
     * @param topic the topic to send the message to
     * @param key the message key
     * @param message the message to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return the send result
     * @throws Exception if sending fails
     */
    <K, T> SendResult<K, T> send(String topic, K key, T message) throws Exception;
    
    /**
     * Send a message to a specific partition synchronously.
     * 
     * @param topic the topic to send the message to
     * @param partition the partition to send to
     * @param key the message key
     * @param message the message to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return the send result
     * @throws Exception if sending fails
     */
    <K, T> SendResult<K, T> send(String topic, Integer partition, K key, T message) throws Exception;
    
    /**
     * Send a message with headers synchronously.
     * 
     * @param topic the topic to send the message to
     * @param key the message key
     * @param message the message to send
     * @param headers the message headers
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return the send result
     * @throws Exception if sending fails
     */
    <K, T> SendResult<K, T> send(String topic, K key, T message, Map<String, Object> headers) throws Exception;
    
    /**
     * Send a ProducerRecord synchronously.
     * 
     * @param record the producer record to send
     * @param <K> the type of the key
     * @param <T> the type of the message
     * @return the send result
     * @throws Exception if sending fails
     */
    <K, T> SendResult<K, T> send(ProducerRecord<K, T> record) throws Exception;
}
