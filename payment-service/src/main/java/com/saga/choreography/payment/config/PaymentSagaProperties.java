package com.saga.choreography.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "saga.payment")
@Getter
@Setter
public class PaymentSagaProperties {

    /**
     * When true, every payment attempt is declined (for compensating-flow demos).
     */
    private boolean simulateFailure = false;
}
