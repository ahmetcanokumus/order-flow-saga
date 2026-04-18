package com.saga.choreography.common.tracing;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Puts the business trace id into SLF4J MDC so log patterns can include it (e.g. %X{traceId}).
 */
@UtilityClass
public class MdcTraceBridge {

    public static final String MDC_TRACE_ID = "traceId";

    public static void put(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(MDC_TRACE_ID, traceId);
        }
    }

    public static Optional<String> currentTraceId() {
        return Optional.ofNullable(MDC.get(MDC_TRACE_ID));
    }

    public static void clearTraceId() {
        MDC.remove(MDC_TRACE_ID);
    }
}
