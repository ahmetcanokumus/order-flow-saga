package com.saga.choreography.common.tracing;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TraceKafkaHeaders {

    public static final String TRACE_ID = "X-Trace-Id";

    public static final String W3C_TRACEPARENT = "traceparent";
}
