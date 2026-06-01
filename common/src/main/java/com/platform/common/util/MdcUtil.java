package com.platform.common.util;

import com.platform.common.model.EventEnvelope;
import com.platform.common.model.EventMetadata;
import org.slf4j.MDC;

public class MdcUtil {

    public static void syncMdc(EventEnvelope envelope) {
        if (envelope == null)
            return;

        if (envelope.getEventId() != null) {
            MDC.put(MdcConstants.EVENT_ID, envelope.getEventId().toString());
        }

        if (envelope.getMetadata() != null) {
            syncMdc(envelope.getMetadata());
        }
    }

    public static void syncMdc(EventMetadata metadata) {
        if (metadata == null)
            return;

        putIfNotNull(MdcConstants.CORRELATION_ID, metadata.getCorrelationId());
        putIfNotNull(MdcConstants.TRACE_ID, metadata.getTraceId());
        putIfNotNull(MdcConstants.BATCH_ID, metadata.getBatchId());

        if (metadata.getRetryCount() != null) {
            MDC.put(MdcConstants.RETRY_COUNT, String.valueOf(metadata.getRetryCount()));
        }
    }

    private static void putIfNotNull(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }
}
