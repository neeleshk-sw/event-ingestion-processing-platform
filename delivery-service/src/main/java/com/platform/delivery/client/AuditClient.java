package com.platform.delivery.client;

import com.platform.common.model.AuditRecord;
import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Component
public class AuditClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditClient.class);

    private static final String MDC_KEY = "correlationId";
    private static final String HEADER = "X-Correlation-Id";

    private final RestClient restClient;

    public AuditClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8088")
                .build();
    }

    public void audit(EventEnvelope event, String stage, String action, String details) {
        try {
            AuditRecord record = new AuditRecord();
            record.setEventId(event.getEventId());
            record.setCorrelationId(event.getMetadata().getCorrelationId());
            record.setStage(stage);
            record.setAction(action);
            record.setTimestamp(Instant.now());
            record.setDetails(details);

            if (event.getMetadata() != null) {
                record.setBatchId(event.getMetadata().getBatchId());
                record.setTraceId(event.getMetadata().getTraceId());
                record.setRetryCount(event.getMetadata().getRetryCount());
                record.setSource(event.getMetadata().getSource());
            }

            restClient.post()
                    .uri("/audit")
                    .header(HEADER, record.getCorrelationId())
                    .body(record)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception ex) {
            logger.warn("Audit logging failed (ignored): {}", ex.getMessage());
        }
    }

    public void audit(String stage, String action, String details, String eventId) {

        try {
            AuditRecord record = new AuditRecord();
            record.setEventId(java.util.UUID.fromString(eventId));
            record.setCorrelationId(MDC.get(MDC_KEY));
            record.setStage(stage);
            record.setAction(action);
            record.setTimestamp(Instant.now());
            record.setDetails(details);

            restClient.post()
                    .uri("/audit")
                    .header(HEADER, MDC.get(MDC_KEY))
                    .body(record)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception ex) {
            // DO NOT BREAK FLOW
            logger.warn("Audit logging failed (ignored): {}", ex.getMessage());
        }
    }
}
