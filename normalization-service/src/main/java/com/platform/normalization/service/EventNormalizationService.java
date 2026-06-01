package com.platform.normalization.service;

import com.platform.common.contract.NormalizationResult;
import com.platform.common.model.EventEnvelope;
import com.platform.normalization.client.AuditClient;
import com.platform.normalization.client.EnrichmentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventNormalizationService {

    private static final Logger logger =
            LoggerFactory.getLogger(EventNormalizationService.class);

    private final AuditClient auditClient;
    private final EnrichmentClient enrichmentClient;

    public EventNormalizationService(AuditClient auditClient,
                                     EnrichmentClient enrichmentClient) {
        this.auditClient = auditClient;
        this.enrichmentClient = enrichmentClient;
    }

    public NormalizationResult normalize(EventEnvelope eventEnvelope) {

        logger.info("Normalizing event");
        // Added: audit normalization start
        auditClient.audit(
                "NORMALIZATION",
                "STARTED",
                "Normalization started",
                eventEnvelope.getEventId().toString()
        );

        EventEnvelope enriched =
                enrichmentClient.enrich(eventEnvelope);

        // Added: audit normalization completion
        auditClient.audit(
                "NORMALIZATION",
                "COMPLETED",
                "Event normalized and forwarded to enrichment",
                eventEnvelope.getEventId().toString()
        );

        return new NormalizationResult(enriched);
    }
}
