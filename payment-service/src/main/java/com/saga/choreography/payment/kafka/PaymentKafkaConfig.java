package com.saga.choreography.payment.kafka;

import com.saga.choreography.common.event.StockReservedEvent;
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
public class PaymentKafkaConfig {

    @Bean
    ConsumerFactory<String, StockReservedEvent> stockReservedConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, StockReservedEvent.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.saga.choreography.common.event");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, StockReservedEvent> stockReservedKafkaListenerContainerFactory(
            ConsumerFactory<String, StockReservedEvent> stockReservedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, StockReservedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stockReservedConsumerFactory);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("payment-kafka-");
        executor.setVirtualThreads(true);
        factory.getContainerProperties().setListenerTaskExecutor(executor);
        return factory;
    }
}
