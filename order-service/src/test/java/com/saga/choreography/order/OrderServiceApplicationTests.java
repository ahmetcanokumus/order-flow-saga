package com.saga.choreography.order;

import com.saga.choreography.common.SagaTopics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Locale;

@SpringBootTest(classes = OrderServiceApplication.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                SagaTopics.ORDER_CREATED,
                SagaTopics.PAYMENT_PROCESSED,
                SagaTopics.PAYMENT_FAILED
        }
)
class OrderServiceApplicationTests {

    static {
        // Embedded Kafka fails on Turkish default locale ("CLASSIC" -> "CLASSİC").
        Locale.setDefault(Locale.ROOT);
    }

    @Test
    void contextLoads() {
    }
}
