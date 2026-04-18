package com.saga.choreography.order.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TraceIdSupplier {

    private final Tracer tracer;

    /**
     * Prefer the active Micrometer/Brave trace id (HTTP ingress); otherwise generate a new correlation id.
     */
    public String currentOrNew() {
        Span current = tracer.currentSpan();
        if (current != null && current.context() != null && current.context().traceId() != null) {
            return current.context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
