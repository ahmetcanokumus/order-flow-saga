package com.saga.choreography.stock.kafka;

import com.saga.choreography.common.event.OrderCreatedEvent;
import com.saga.choreography.common.event.PaymentFailedEvent;
import com.saga.choreography.common.event.PaymentProcessedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class StockKafkaConfig {

    @Bean
    ConsumerFactory<String, OrderCreatedEvent> orderCreatedConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerProps(kafkaProperties, OrderCreatedEvent.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> orderCreatedConsumerFactory
    ) {
        return virtualThreadFactory(orderCreatedConsumerFactory);
    }

    @Bean
    ConsumerFactory<String, PaymentProcessedEvent> paymentProcessedConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerProps(kafkaProperties, PaymentProcessedEvent.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> paymentProcessedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentProcessedEvent> paymentProcessedConsumerFactory
    ) {
        return virtualThreadFactory(paymentProcessedConsumerFactory);
    }

    @Bean
    ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerProps(kafkaProperties, PaymentFailedEvent.class));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent> paymentFailedKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentFailedEvent> paymentFailedConsumerFactory
    ) {
        return virtualThreadFactory(paymentFailedConsumerFactory);
    }

    private static Map<String, Object> consumerProps(KafkaProperties kafkaProperties, Class<?> valueType) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.saga.choreography.common.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return props;
    }

    private static <T> ConcurrentKafkaListenerContainerFactory<String, T> virtualThreadFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("stock-kafka-");
        executor.setVirtualThreads(true);
        factory.getContainerProperties().setListenerTaskExecutor(executor);
        return factory;
    }
}
