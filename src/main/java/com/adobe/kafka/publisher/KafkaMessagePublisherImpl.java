package com.adobe.kafka.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of KafkaMessagePublisher using Spring Kafka's KafkaTemplate.
 */
@Component
public class KafkaMessagePublisherImpl implements KafkaMessagePublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessagePublisherImpl.class);
    
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    
    public KafkaMessagePublisherImpl(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<SendResult<String, T>> sendAsync(String topic, T message) {
        logger.debug("Sending message to topic: {}", topic);
        return (CompletableFuture<SendResult<String, T>>) (CompletableFuture<?>) kafkaTemplate.send(topic, message);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, K key, T message) {
        logger.debug("Sending message with key to topic: {} with key: {}", topic, key);
        return (CompletableFuture<SendResult<K, T>>) (CompletableFuture<?>) kafkaTemplate.send(topic, key, message);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, Integer partition, K key, T message) {
        logger.debug("Sending message to topic: {} partition: {} with key: {}", topic, partition, key);
        return (CompletableFuture<SendResult<K, T>>) (CompletableFuture<?>) kafkaTemplate.send(topic, partition, key, message);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, T> CompletableFuture<SendResult<K, T>> sendAsync(String topic, K key, T message, Map<String, Object> headers) {
        logger.debug("Sending message with headers to topic: {} with key: {}", topic, key);
        ProducerRecord<Object, Object> record = createRecordWithHeaders(topic, key, message, headers);
        return (CompletableFuture<SendResult<K, T>>) (CompletableFuture<?>) kafkaTemplate.send(record);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <K, T> CompletableFuture<SendResult<K, T>> sendAsync(ProducerRecord<K, T> record) {
        logger.debug("Sending ProducerRecord to topic: {}", record.topic());
        // Convert to Object types for KafkaTemplate
        ProducerRecord<Object, Object> objRecord = new ProducerRecord<>(
                record.topic(), record.partition(), record.timestamp(), 
                record.key(), record.value(), record.headers());
        return (CompletableFuture<SendResult<K, T>>) (CompletableFuture<?>) kafkaTemplate.send(objRecord);
    }
    
    @Override
    public <T> SendResult<String, T> send(String topic, T message) throws Exception {
        logger.debug("Sending message synchronously to topic: {}", topic);
        return sendAsync(topic, message).get();
    }
    
    @Override
    public <K, T> SendResult<K, T> send(String topic, K key, T message) throws Exception {
        logger.debug("Sending message with key synchronously to topic: {} with key: {}", topic, key);
        return sendAsync(topic, key, message).get();
    }
    
    @Override
    public <K, T> SendResult<K, T> send(String topic, Integer partition, K key, T message) throws Exception {
        logger.debug("Sending message synchronously to topic: {} partition: {} with key: {}", topic, partition, key);
        return sendAsync(topic, partition, key, message).get();
    }
    
    @Override
    public <K, T> SendResult<K, T> send(String topic, K key, T message, Map<String, Object> headers) throws Exception {
        logger.debug("Sending message with headers synchronously to topic: {} with key: {}", topic, key);
        return sendAsync(topic, key, message, headers).get();
    }
    
    @Override
    public <K, T> SendResult<K, T> send(ProducerRecord<K, T> record) throws Exception {
        logger.debug("Sending ProducerRecord synchronously to topic: {}", record.topic());
        return sendAsync(record).get();
    }
    
    /**
     * Creates a ProducerRecord with headers.
     * 
     * @param topic the topic name
     * @param key the message key
     * @param message the message value
     * @param headers the headers map
     * @return a ProducerRecord with the specified headers
     */
    private ProducerRecord<Object, Object> createRecordWithHeaders(String topic, Object key, Object message, Map<String, Object> headers) {
        List<Header> recordHeaders = new ArrayList<>();
        if (headers != null) {
            headers.forEach((headerKey, headerValue) -> {
                byte[] headerBytes = headerValue != null ? headerValue.toString().getBytes() : null;
                recordHeaders.add(new RecordHeader(headerKey, headerBytes));
            });
        }
        return new ProducerRecord<>(topic, null, key, message, recordHeaders);
    }
}
