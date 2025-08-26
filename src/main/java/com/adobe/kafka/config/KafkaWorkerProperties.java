package com.adobe.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the Kafka Worker library.
 * These properties can be configured in application.yml or application.properties.
 */
@ConfigurationProperties(prefix = "adobe.kafka")
public class KafkaWorkerProperties {
    
    /**
     * Kafka bootstrap servers.
     */
    private String bootstrapServers = "localhost:9092";
    
    /**
     * Default consumer group id.
     */
    private String defaultConsumerGroup = "kafka-worker-group";
    
    /**
     * Producer configuration properties.
     */
    private Producer producer = new Producer();
    
    /**
     * Consumer configuration properties.
     */
    private Consumer consumer = new Consumer();
    
    /**
     * Additional Kafka properties.
     */
    private Map<String, String> properties = new HashMap<>();
    
    public String getBootstrapServers() {
        return bootstrapServers;
    }
    
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    
    public String getDefaultConsumerGroup() {
        return defaultConsumerGroup;
    }
    
    public void setDefaultConsumerGroup(String defaultConsumerGroup) {
        this.defaultConsumerGroup = defaultConsumerGroup;
    }
    
    public Producer getProducer() {
        return producer;
    }
    
    public void setProducer(Producer producer) {
        this.producer = producer;
    }
    
    public Consumer getConsumer() {
        return consumer;
    }
    
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
    /**
     * Producer-specific configuration properties.
     */
    public static class Producer {
        
        /**
         * Key serializer class.
         */
        private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";
        
        /**
         * Value serializer class.
         */
        private String valueSerializer = "org.springframework.kafka.support.serializer.JsonSerializer";
        
        /**
         * Acknowledgment mode.
         */
        private String acks = "all";
        
        /**
         * Number of retries.
         */
        private int retries = 3;
        
        /**
         * Batch size.
         */
        private int batchSize = 16384;
        
        /**
         * Linger time in milliseconds.
         */
        private int lingerMs = 5;
        
        /**
         * Buffer memory.
         */
        private long bufferMemory = 33554432L;
        
        /**
         * Compression type.
         */
        private String compressionType = "snappy";
        
        /**
         * Enable idempotence.
         */
        private boolean enableIdempotence = true;
        
        /**
         * Max in-flight requests per connection.
         */
        private int maxInFlightRequestsPerConnection = 5;
        
        /**
         * Request timeout in milliseconds.
         */
        private int requestTimeoutMs = 30000;
        
        /**
         * Additional producer properties.
         */
        private Map<String, String> properties = new HashMap<>();
        
        // Getters and setters
        public String getKeySerializer() { return keySerializer; }
        public void setKeySerializer(String keySerializer) { this.keySerializer = keySerializer; }
        
        public String getValueSerializer() { return valueSerializer; }
        public void setValueSerializer(String valueSerializer) { this.valueSerializer = valueSerializer; }
        
        public String getAcks() { return acks; }
        public void setAcks(String acks) { this.acks = acks; }
        
        public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }
        
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
        
        public int getLingerMs() { return lingerMs; }
        public void setLingerMs(int lingerMs) { this.lingerMs = lingerMs; }
        
        public long getBufferMemory() { return bufferMemory; }
        public void setBufferMemory(long bufferMemory) { this.bufferMemory = bufferMemory; }
        
        public String getCompressionType() { return compressionType; }
        public void setCompressionType(String compressionType) { this.compressionType = compressionType; }
        
        public boolean isEnableIdempotence() { return enableIdempotence; }
        public void setEnableIdempotence(boolean enableIdempotence) { this.enableIdempotence = enableIdempotence; }
        
        public int getMaxInFlightRequestsPerConnection() { return maxInFlightRequestsPerConnection; }
        public void setMaxInFlightRequestsPerConnection(int maxInFlightRequestsPerConnection) { 
            this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection; 
        }
        
        public int getRequestTimeoutMs() { return requestTimeoutMs; }
        public void setRequestTimeoutMs(int requestTimeoutMs) { this.requestTimeoutMs = requestTimeoutMs; }
        
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }
    
    /**
     * Consumer-specific configuration properties.
     */
    public static class Consumer {
        
        /**
         * Key deserializer class.
         */
        private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
        
        /**
         * Value deserializer class.
         */
        private String valueDeserializer = "org.springframework.kafka.support.serializer.JsonDeserializer";
        
        /**
         * Auto offset reset strategy.
         */
        private String autoOffsetReset = "earliest";
        
        /**
         * Enable auto commit.
         */
        private boolean enableAutoCommit = false;
        
        /**
         * Session timeout in milliseconds.
         */
        private int sessionTimeoutMs = 30000;
        
        /**
         * Heartbeat interval in milliseconds.
         */
        private int heartbeatIntervalMs = 3000;
        
        /**
         * Max poll records.
         */
        private int maxPollRecords = 500;
        
        /**
         * Max poll interval in milliseconds.
         */
        private int maxPollIntervalMs = 300000;
        
        /**
         * Fetch min bytes.
         */
        private int fetchMinBytes = 1;
        
        /**
         * Fetch max wait in milliseconds.
         */
        private int fetchMaxWaitMs = 500;
        
        /**
         * Additional consumer properties.
         */
        private Map<String, String> properties = new HashMap<>();
        
        // Getters and setters
        public String getKeyDeserializer() { return keyDeserializer; }
        public void setKeyDeserializer(String keyDeserializer) { this.keyDeserializer = keyDeserializer; }
        
        public String getValueDeserializer() { return valueDeserializer; }
        public void setValueDeserializer(String valueDeserializer) { this.valueDeserializer = valueDeserializer; }
        
        public String getAutoOffsetReset() { return autoOffsetReset; }
        public void setAutoOffsetReset(String autoOffsetReset) { this.autoOffsetReset = autoOffsetReset; }
        
        public boolean isEnableAutoCommit() { return enableAutoCommit; }
        public void setEnableAutoCommit(boolean enableAutoCommit) { this.enableAutoCommit = enableAutoCommit; }
        
        public int getSessionTimeoutMs() { return sessionTimeoutMs; }
        public void setSessionTimeoutMs(int sessionTimeoutMs) { this.sessionTimeoutMs = sessionTimeoutMs; }
        
        public int getHeartbeatIntervalMs() { return heartbeatIntervalMs; }
        public void setHeartbeatIntervalMs(int heartbeatIntervalMs) { this.heartbeatIntervalMs = heartbeatIntervalMs; }
        
        public int getMaxPollRecords() { return maxPollRecords; }
        public void setMaxPollRecords(int maxPollRecords) { this.maxPollRecords = maxPollRecords; }
        
        public int getMaxPollIntervalMs() { return maxPollIntervalMs; }
        public void setMaxPollIntervalMs(int maxPollIntervalMs) { this.maxPollIntervalMs = maxPollIntervalMs; }
        
        public int getFetchMinBytes() { return fetchMinBytes; }
        public void setFetchMinBytes(int fetchMinBytes) { this.fetchMinBytes = fetchMinBytes; }
        
        public int getFetchMaxWaitMs() { return fetchMaxWaitMs; }
        public void setFetchMaxWaitMs(int fetchMaxWaitMs) { this.fetchMaxWaitMs = fetchMaxWaitMs; }
        
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
    }
}
