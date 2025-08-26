# Kafka Worker Library

A Spring Boot-based Kafka worker library that provides simple publisher and subscriber functionality for Apache Kafka. This library allows teams to easily integrate Kafka messaging into their applications without dealing with the complexities of Kafka configuration and management.

## Features

- **Easy Integration**: Auto-configuration for Spring Boot applications
- **Multiple Handler Types**: Support for basic, typed, error-aware, and batch message handlers
- **Flexible Publishing**: Synchronous and asynchronous message publishing with various options
- **Error Handling**: Built-in retry logic and error handling capabilities
- **Configuration**: Comprehensive configuration options for both producers and consumers
- **Type Safety**: Generic interfaces for type-safe message handling

## Requirements

- Java 17 or higher
- Spring Boot 3.5.0
- Apache Kafka 2.8+ (compatible with spring-kafka 3.3.0)

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.adobe</groupId>
    <artifactId>kafka-worker</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### 1. Configuration

Add Kafka configuration to your `application.yml`:

```yaml
adobe:
  kafka:
    bootstrap-servers: localhost:9092
    default-consumer-group: your-app-group
    producer:
      acks: all
      retries: 3
    consumer:
      auto-offset-reset: earliest
      enable-auto-commit: false
```

### 2. Publishing Messages

Inject `KafkaMessagePublisher` and start publishing:

```java
@Service
public class MessageService {
    
    private final KafkaMessagePublisher publisher;
    
    public MessageService(KafkaMessagePublisher publisher) {
        this.publisher = publisher;
    }
    
    public void sendMessage(String topic, Object message) {
        // Async publishing
        publisher.sendAsync(topic, message)
                .thenAccept(result -> log.info("Message sent: {}", result))
                .exceptionally(throwable -> {
                    log.error("Failed to send message", throwable);
                    return null;
                });
    }
    
    public void sendKeyedMessage(String topic, String key, Object message) throws Exception {
        // Sync publishing
        SendResult<String, Object> result = publisher.send(topic, key, message);
        log.info("Message sent synchronously: {}", result);
    }
}
```

### 3. Subscribing to Messages

Inject `KafkaMessageSubscriber` and register handlers:

```java
@Service
public class MessageSubscriberService {
    
    private final KafkaMessageSubscriber subscriber;
    
    public MessageSubscriberService(KafkaMessageSubscriber subscriber) {
        this.subscriber = subscriber;
    }
    
    @PostConstruct
    public void setupSubscriptions() {
        // Basic message handler
        MessageHandler<String, String> handler = record -> {
            log.info("Received: {}", record.value());
        };
        
        subscriber.subscribe("my-topic", handler);
        
        // Typed message handler
        TypedMessageHandler<String, UserEvent> typedHandler = 
            (key, value, topic, partition, offset) -> {
                log.info("User event: {}", value);
                processUserEvent(value);
            };
        
        subscriber.subscribe("user-events", typedHandler);
    }
    
    private void processUserEvent(UserEvent event) {
        // Process the event
    }
}
```

## Message Handler Types

### 1. Basic Message Handler

```java
MessageHandler<String, String> handler = record -> {
    String key = record.key();
    String value = record.value();
    String topic = record.topic();
    // Process the message
};

subscriber.subscribe("topic", handler);
```

### 2. Typed Message Handler

```java
TypedMessageHandler<String, UserEvent> handler = 
    (key, value, topic, partition, offset) -> {
        // Direct access to typed values
        processUserEvent(value);
    };

subscriber.subscribe("user-events", handler);
```

### 3. Error-Aware Message Handler

```java
ErrorAwareMessageHandler<String, OrderEvent> handler = new ErrorAwareMessageHandler<>() {
    @Override
    public void handle(ConsumerRecord<String, OrderEvent> record) throws Exception {
        // Process the message - may throw exceptions
        processOrder(record.value());
    }
    
    @Override
    public void handleError(ConsumerRecord<String, OrderEvent> record, Exception exception) {
        // Handle processing errors
        log.error("Failed to process order: {}", exception.getMessage());
        sendToDeadLetterQueue(record);
    }
    
    @Override
    public boolean shouldRetry(ConsumerRecord<String, OrderEvent> record, Exception exception, int attemptCount) {
        // Custom retry logic
        return attemptCount < 3 && !(exception instanceof ValidationException);
    }
};

subscriber.subscribe("orders", handler);
```

### 4. Batch Message Handler

```java
BatchMessageHandler<String, String> batchHandler = records -> {
    // Process multiple messages at once
    for (ConsumerRecord<String, String> record : records) {
        processMessage(record);
    }
    // Commit all at once
};

subscriber.subscribeBatch("bulk-topic", batchHandler);
```

## Publishing Options

### Basic Publishing

```java
// Simple message
publisher.sendAsync("topic", "Hello World");

// With key
publisher.sendAsync("topic", "user-123", userEvent);

// To specific partition
publisher.sendAsync("topic", 2, "key", message);

// With headers
Map<String, Object> headers = Map.of("source", "user-service");
publisher.sendAsync("topic", "key", message, headers);
```

### Synchronous Publishing

```java
try {
    SendResult<String, String> result = publisher.send("topic", "message");
    log.info("Message sent: offset={}", result.getRecordMetadata().offset());
} catch (Exception e) {
    log.error("Failed to send message", e);
}
```

## Configuration Options

### Producer Configuration

```yaml
adobe:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 5
      buffer-memory: 33554432
      compression-type: snappy
      enable-idempotence: true
      max-in-flight-requests-per-connection: 5
      request-timeout-ms: 30000
      properties:
        max.request.size: 1048576
```

### Consumer Configuration

```yaml
adobe:
  kafka:
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      session-timeout-ms: 30000
      heartbeat-interval-ms: 3000
      max-poll-records: 500
      max-poll-interval-ms: 300000
      fetch-min-bytes: 1
      fetch-max-wait-ms: 500
      properties:
        spring.json.trusted.packages: "com.yourcompany.model"
```

## Subscription Management

```java
// Subscribe and get subscription ID
String subscriptionId = subscriber.subscribe("topic", handler);

// Pause consumption
subscriber.pause(subscriptionId);

// Resume consumption
subscriber.resume(subscriptionId);

// Check if active
boolean isActive = subscriber.isActive(subscriptionId);

// Get all active subscriptions
List<String> activeSubscriptions = subscriber.getActiveSubscriptions();

// Unsubscribe
subscriber.unsubscribe(subscriptionId);
```

## Best Practices

### 1. Message Serialization

Use JSON serialization for complex objects:

```java
public class UserEvent {
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    // constructors, getters, setters
}
```

### 2. Error Handling

Always implement proper error handling:

```java
ErrorAwareMessageHandler<String, MyEvent> handler = new ErrorAwareMessageHandler<>() {
    @Override
    public void handle(ConsumerRecord<String, MyEvent> record) throws Exception {
        // Your processing logic
    }
    
    @Override
    public void handleError(ConsumerRecord<String, MyEvent> record, Exception exception) {
        // Log error and send to DLQ
        sendToDeadLetterQueue(record, exception);
    }
};
```

### 3. Consumer Groups

Use different consumer groups for different services:

```java
subscriber.subscribe("user-events", "notification-service", notificationHandler);
subscriber.subscribe("user-events", "analytics-service", analyticsHandler);
```

### 4. Monitoring

Monitor your subscriptions:

```java
@Scheduled(fixedRate = 60000)
public void monitorSubscriptions() {
    List<String> activeSubscriptions = subscriber.getActiveSubscriptions();
    log.info("Active subscriptions: {}", activeSubscriptions.size());
}
```

## Centralized Logging and Exception Tracking

**🚀 AUTOMATIC TRACKABLE LOGGING!** The library provides a comprehensive logging system that requires ZERO client configuration. Just add the library and get complete message traceability out-of-the-box.

### Key Features

- **🔄 Automatic Setup**: Zero configuration required - logging works immediately
- **🏷️ Unique Message IDs**: Each message gets a trackable ID across all log entries
- **📝 Built-in Appenders**: Console, file, error, and JSON logging pre-configured
- **🔍 Complete Context**: All logs include topic, partition, offset, key, subscription info
- **⚡ Production Ready**: Async appenders, rolling files, error separation
- **🎯 Trackable Exceptions**: Specialized exceptions with automatic context capture
- **🔗 Cross-cutting Traceability**: All related logs share the same message tracking ID

### Using the ExceptionHelper

The `ExceptionHelper` class provides the primary interface for handlers to integrate with the centralized logging system:

```java
import com.adobe.kafka.logging.ExceptionHelper;

MessageHandler<String, String> handler = record -> {
    // Log information with automatic message context
    ExceptionHelper.logInfo("Processing message: " + record.value());
    
    // Throw trackable exceptions with automatic context capture
    if (record.value().contains("invalid")) {
        ExceptionHelper.throwTrackable("Invalid message content detected");
    }
    
    // Specialized exception types for different scenarios
    if (record.key() == null) {
        ExceptionHelper.throwValidationError("messageKey", null, "Key cannot be null");
    }
    
    // Business rule violations
    if (!isValidUser(record.key())) {
        ExceptionHelper.throwBusinessRuleViolation("Valid user required", "Key: " + record.key());
    }
    
    // External service integration errors
    try {
        callExternalService(record.value());
    } catch (Exception e) {
        ExceptionHelper.throwServiceIntegrationError("UserService", "validateUser", e);
    }
};
```

### Automatic Context Management

All message processing is automatically wrapped with context management. Every log entry will include:

- **messageId**: Unique identifier for the message processing session
- **topic**: Kafka topic name
- **partition**: Partition number
- **offset**: Message offset
- **messageKey**: The message key (if present)
- **subscriptionId**: The subscription processing this message
- **handlerType**: Type of handler processing the message

### Example Log Output

With centralized logging, your logs will look like this:

```
2024-01-15 10:30:45.123 INFO  [messageId=msg-a1b2c3d4, topic=user-events, partition=0, offset=12345, messageKey=user-123, subscriptionId=sub-xyz, handlerType=MessageHandler] Processing message: {"userId":"user-123","action":"login"}

2024-01-15 10:30:45.125 ERROR [messageId=msg-a1b2c3d4, topic=user-events, partition=0, offset=12345, messageKey=user-123, subscriptionId=sub-xyz, handlerType=MessageHandler] Trackable exception occurred - Context: Message processing - Error: Business rule violation: Valid user required - Context: Key: invalid-user
```

### Error-Aware Handler Integration

The `ErrorAwareMessageHandler` automatically integrates with centralized logging:

```java
ErrorAwareMessageHandler<String, OrderEvent> handler = new ErrorAwareMessageHandler<>() {
    @Override
    public void handle(ConsumerRecord<String, OrderEvent> record) throws Exception {
        // All processing is automatically tracked
        ExceptionHelper.logInfo("Processing order: " + record.value().getOrderId());
        
        if (!isValidOrder(record.value())) {
            ExceptionHelper.throwValidationError("order.amount", 
                record.value().getAmount(), "Amount must be positive");
        }
        
        processOrder(record.value());
    }
    
    @Override
    public void handleError(ConsumerRecord<String, OrderEvent> record, Exception exception) {
        // Default implementation uses centralized logging
        // Add custom error handling if needed
        ExceptionHelper.logWarning("Order processing failed, sending to DLQ");
        super.handleError(record, exception); // This logs with full context
    }
    
    @Override
    public boolean shouldRetry(ConsumerRecord<String, OrderEvent> record, Exception exception, int attemptCount) {
        // Don't retry trackable exceptions (they're logged with full context)
        if (exception instanceof TrackableKafkaException) {
            return false;
        }
        return attemptCount < 3;
    }
};
```

### Accessing Current Context

Handlers can access the current message context at any time:

```java
MessageHandler<String, String> handler = record -> {
    String messageId = ExceptionHelper.getCurrentMessageId();
    String topic = ExceptionHelper.getCurrentTopic();
    
    ExceptionHelper.logInfo("Current processing context - ID: " + messageId + ", Topic: " + topic);
    
    // Include message ID in external API calls for traceability
    callExternalAPI(record.value(), messageId);
};
```

### Exception Types

The system provides several specialized exception types:

```java
// General trackable exception
ExceptionHelper.throwTrackable("Something went wrong");

// Validation errors
ExceptionHelper.throwValidationError("fieldName", value, "reason");

// Business rule violations
ExceptionHelper.throwBusinessRuleViolation("rule description", "context");

// Data processing errors
ExceptionHelper.throwDataProcessingError("operation", "data description", cause);

// External service integration errors
ExceptionHelper.throwServiceIntegrationError("ServiceName", "operation", cause);

// Retry exhaustion
ExceptionHelper.throwRetryExhausted("operation", attemptCount, lastError);
```

### Benefits

1. **Complete Traceability**: Every log entry for a message contains the same tracking ID
2. **Error Context**: Exceptions automatically capture all relevant message information
3. **Simplified API**: Handlers just call `ExceptionHelper` methods
4. **Automatic Cleanup**: MDC context is automatically cleaned up after processing
5. **Performance**: Minimal overhead with efficient context management
6. **Integration**: Works seamlessly with existing logging frameworks and tools

## Automatic Client Logger Integration

**🎯 ZERO CONFIGURATION REQUIRED!** The Kafka Worker Library provides built-in logging configurations that automatically handle MDC context formatting. Client applications get trackable message logging out-of-the-box with no setup required.

### How It Works

1. **Automatic Configuration**: The library includes default `logback.xml` and `logback-spring.xml` configurations
2. **MDC Context**: All client loggers automatically include message tracking context  
3. **Multiple Outputs**: Logs are automatically written to console, files, and error-specific files
4. **Production Ready**: Includes rolling file policies, async appenders, and performance optimizations

When you add this library to your project, you automatically get:

- **📝 Console Logging**: Color-coded output with message context for development
- **📄 File Logging**: Rolling log files with complete MDC context  
- **🚨 Error Files**: Separate error logs with comprehensive context and stack traces
- **⚡ Async Processing**: Performance-optimized async appenders
- **📊 Structured Logs**: Optional JSON logging for cloud/container environments
- **🔧 Spring Boot Integration**: Profile-based configuration and properties integration

### Simple Client Code Example

Your handlers just use standard SLF4J loggers - MDC context is automatically included:

```java
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public void processUser(String userId, String userData) {
        // This log automatically includes: messageId, topic, partition, offset, etc.
        logger.info("Processing user: {}", userId);
        
        try {
            validateUser(userId);
            updateUserData(userData);
            
            // All logs within the message processing context include MDC
            logger.info("User processing completed successfully");
            
        } catch (Exception e) {
            // Error logs also include the full message context
            logger.error("User processing failed", e);
            throw e;
        }
    }
    
    private void validateUser(String userId) {
        logger.debug("Validating user: {}", userId);
        // Validation logic...
    }
    
    private void updateUserData(String data) {
        logger.debug("Updating user data");
        // Update logic...
    }
}
```

### Automatic Log Output

All logs from your services automatically include message tracking context:

```
10:30:45.123 INFO  [msg-a1b2c3d4,user-events] [           main] com.mycompany.UserService        : Processing user: user-123

10:30:45.125 DEBUG [msg-a1b2c3d4,user-events] [           main] com.mycompany.UserService        : Validating user: user-123

10:30:45.127 INFO  [msg-a1b2c3d4,user-events] [           main] com.mycompany.UserService        : User processing completed successfully
```

### File Logs with Full Context

Your application logs are automatically written to files with complete context:

```
2024-01-15 10:30:45.123 [main] INFO  [messageId=msg-a1b2c3d4, topic=user-events, partition=0, offset=12345, key=user-123, subscription=sub-xyz, handler=MessageHandler] com.mycompany.UserService - Processing user: user-123
```

### Generated Log Files

The library automatically creates and manages these log files:

- **`{app-name}.log`** - All application logs with full MDC context
- **`{app-name}-error.log`** - Warning and error logs with comprehensive troubleshooting context  
- **`{app-name}-kafka.log`** - Structured Kafka-specific logs for analysis
- **`{app-name}-json.log`** - JSON formatted logs (when using Spring profiles: `cloud`, `kubernetes`, `prod`)

### Configuration Properties

You can customize the logging behavior using standard Spring Boot properties:

```yaml
# application.yml
spring:
  application:
    name: my-kafka-app  # Used in log file names

logging:
  file:
    path: ./logs  # Directory for log files (default: logs/)
  level:
    com.adobe.kafka: DEBUG  # Library log level
    com.mycompany: INFO     # Your application log level
```

### No Configuration Required

**That's it!** Just add the library dependency and start using it. Your logs will automatically include message tracking context without any additional setup.

### Available MDC Keys

The following MDC keys are automatically available in all client logs during message processing:

- **`messageId`**: Unique message processing identifier (e.g., `msg-a1b2c3d4`)
- **`topic`**: Kafka topic name
- **`partition`**: Partition number
- **`offset`**: Message offset
- **`messageKey`**: Message key (shows `-` if not present)
- **`subscriptionId`**: Subscription ID processing the message
- **`handlerType`**: Handler type (MessageHandler, ErrorAwareMessageHandler, etc.)

### Utility Methods for Enhanced Logging

The library provides utility methods for accessing MDC context in your code:

```java
import com.adobe.kafka.logging.ExceptionHelper;
import com.adobe.kafka.logging.LoggingUtils;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void processData(String data) {
        // Check if we're in a message processing context
        if (ExceptionHelper.isInMessageContext()) {
            String messageId = ExceptionHelper.getCurrentMessageId();
            String topic = ExceptionHelper.getCurrentTopic();
            
            logger.info("Processing data for message {} from topic {}", messageId, topic);
            
            // Include full context in external API calls
            callExternalAPI(data, ExceptionHelper.getCurrentContextSummary());
        } else {
            logger.info("Processing data outside message context");
        }
        
        // Performance logging with context
        LoggingUtils.logPerformance(logger, "data-processing", () -> {
            // Your processing logic here
            performDataProcessing(data);
        });
        
        // Method execution tracing
        LoggingUtils.logMethodExecution(logger, "validateData", () -> {
            validateData(data);
        });
    }
}
```

## Advanced Customization (Optional)

While the library provides complete logging out-of-the-box, you can customize the behavior if needed:

### Override Default Configuration

To override the library's logging configuration, create your own `logback.xml` or `logback-spring.xml` in your application's resources:

```xml
<!-- Your custom logback-spring.xml -->
<configuration>
    <!-- Your custom appenders and loggers -->
    <!-- Remember to include MDC keys: %X{messageId:-}, %X{topic:-}, etc. -->
</configuration>
```

### Extend Default Configuration

To extend (rather than replace) the default configuration, use logback includes:

```xml
<!-- Include the library's default configuration -->
<include resource="logback-spring.xml"/>

<!-- Add your custom appenders -->
<appender name="CUSTOM_APPENDER" class="...">
    <!-- Your custom appender configuration -->
</appender>

<!-- Add custom loggers -->
<logger name="com.mycompany" level="DEBUG">
    <appender-ref ref="CUSTOM_APPENDER" />
</logger>
```

### Accessing Message Context (Optional)

For advanced scenarios, you can optionally access the current message context:

```java
import com.adobe.kafka.logging.ExceptionHelper;

public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);
    
    public void processData(String data) {
        // Just use standard SLF4J logging - MDC context is automatic!
        logger.info("Processing data: {}", data);
        
        // Optional: Access current context for external API calls
        if (ExceptionHelper.isInMessageContext()) {
            String messageId = ExceptionHelper.getCurrentMessageId();
            callExternalAPI(data, messageId);
        }
        
        // All your existing logging works automatically
        performDataProcessing(data);
    }
}
```

### Best Practices

1. **Use Standard SLF4J Loggers**: No special setup required - just create loggers normally
2. **Let the Library Handle MDC**: The built-in configuration includes all necessary context
3. **Access Context When Needed**: Use `ExceptionHelper` methods only for advanced scenarios
4. **Monitor Log Files**: The library creates separate error files for easy troubleshooting
5. **Customize Only When Needed**: The default configuration works for most use cases

## Examples

Check the `examples` package for complete working examples:

- `SimpleMessageExample.java` - Basic publishing and subscribing
- `AdvancedMessageExample.java` - Typed handlers and error handling
- `CentralizedLoggingExample.java` - Complete logging and exception tracking example

## Configuration Files

The library includes these logging configurations (automatically applied):

- **`logback.xml`** - Default logging configuration with MDC support (automatically used)
- **`logback-spring.xml`** - Spring Boot optimized configuration (automatically used)
- **`logback-kafka-mdc.xml`** - Advanced customization example (for reference only)
- **`application-template.yml`** - Kafka worker configuration template

### Automatic Configuration Priority

1. **Your app's `logback-spring.xml`** (if present) - overrides everything
2. **Your app's `logback.xml`** (if present) - overrides library defaults  
3. **Library's `logback-spring.xml`** (Spring Boot apps) - automatic MDC configuration
4. **Library's `logback.xml`** (non-Spring apps) - automatic MDC configuration

## Quick Start Summary

1. **Add Dependency**: Include the kafka-worker library in your `pom.xml`
2. **Configure Kafka**: Set up your `application.yml` with Kafka connection details
3. **Write Handlers**: Create message handlers using standard SLF4J loggers
4. **Run Application**: Start your app - logging with message tracking works automatically!

```java
// That's it! No logging configuration needed.
@Component  
public class MyHandler implements MessageHandler<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(MyHandler.class);
    
    @Override
    public void handle(ConsumerRecord<String, String> record) {
        // Logs automatically include: messageId, topic, partition, offset, key, etc.
        logger.info("Processing message: {}", record.value());
    }
}
```

**Result**: Complete message traceability across your entire application with zero logging configuration! 🎉

## License

This library is proprietary to Adobe Inc.

## Support

For questions and support, please contact the Adobe Sign Platform team.
