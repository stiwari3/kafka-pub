package com.adobe.kafka.config;

import com.adobe.kafka.publisher.KafkaMessagePublisher;
import com.adobe.kafka.publisher.KafkaMessagePublisherImpl;
import com.adobe.kafka.subscriber.KafkaMessageSubscriber;
import com.adobe.kafka.subscriber.KafkaMessageSubscriberImpl;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration class for the Kafka Worker library.
 * This class automatically configures the necessary Kafka beans when the library is included in a Spring Boot application.
 */
@AutoConfiguration(before = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaWorkerProperties.class)
public class KafkaWorkerAutoConfiguration {
    
    private final KafkaWorkerProperties properties;
    
    public KafkaWorkerAutoConfiguration(KafkaWorkerProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Creates a ProducerFactory with the configured properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<Object, Object> kafkaProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.getProducer().getKeySerializer());
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, properties.getProducer().getValueSerializer());
        
        // Producer-specific configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, properties.getProducer().getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, properties.getProducer().getRetries());
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, properties.getProducer().getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, properties.getProducer().getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getProducer().getBufferMemory());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, properties.getProducer().getCompressionType());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, properties.getProducer().isEnableIdempotence());
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, properties.getProducer().getMaxInFlightRequestsPerConnection());
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, properties.getProducer().getRequestTimeoutMs());
        
        // Additional global properties
        properties.getProperties().forEach(configProps::put);
        
        // Additional producer properties
        properties.getProducer().getProperties().forEach(configProps::put);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    /**
     * Creates a KafkaTemplate with the configured ProducerFactory.
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    /**
     * Creates a ConsumerFactory with the configured properties.
     */
    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<Object, Object> kafkaConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Basic configuration
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getDefaultConsumerGroup());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, properties.getConsumer().getKeyDeserializer());
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, properties.getConsumer().getValueDeserializer());
        
        // Consumer-specific configuration
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getConsumer().getAutoOffsetReset());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.getConsumer().isEnableAutoCommit());
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, properties.getConsumer().getSessionTimeoutMs());
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, properties.getConsumer().getHeartbeatIntervalMs());
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getConsumer().getMaxPollRecords());
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, properties.getConsumer().getMaxPollIntervalMs());
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, properties.getConsumer().getFetchMinBytes());
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, properties.getConsumer().getFetchMaxWaitMs());
        
        // Additional global properties
        properties.getProperties().forEach(configProps::put);
        
        // Additional consumer properties
        properties.getConsumer().getProperties().forEach(configProps::put);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    /**
     * Creates the KafkaMessagePublisher bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaMessagePublisher kafkaMessagePublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new KafkaMessagePublisherImpl(kafkaTemplate);
    }
    
    /**
     * Creates the KafkaMessageSubscriber bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public KafkaMessageSubscriber kafkaMessageSubscriber(KafkaListenerEndpointRegistry endpointRegistry) {
        return new KafkaMessageSubscriberImpl(endpointRegistry);
    }
    
    /**
     * Creates a default message listener container factory.
     */
    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    public ConcurrentMessageListenerContainer<Object, Object> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory) {
        
        ContainerProperties containerProperties = new ContainerProperties("dummy-topic");
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        containerProperties.setGroupId(properties.getDefaultConsumerGroup());
        
        ConcurrentMessageListenerContainer<Object, Object> container = 
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setConcurrency(3); // Default concurrency
        
        return container;
    }
}
