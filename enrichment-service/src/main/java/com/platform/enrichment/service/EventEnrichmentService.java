package com.platform.enrichment.service;

import com.platform.common.contract.DeliveryResult;
import com.platform.common.model.EventEnvelope;
import com.platform.enrichment.client.AuditClient;
import com.platform.enrichment.client.RoutingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventEnrichmentService {

    private static final Logger logger =
            LoggerFactory.getLogger(EventEnrichmentService.class);

    private final AuditClient auditClient;
    private final RoutingClient routingClient;

    public EventEnrichmentService(AuditClient auditClient,
                                  RoutingClient routingClient) {
        this.auditClient = auditClient;
        this.routingClient = routingClient;
    }

    public EventEnvelope enrich(EventEnvelope eventEnvelope) {

        logger.info("Applying enrichment logic");
        // Added: audit enrichment start
        auditClient.audit(
                "ENRICHMENT",
                "STARTED",
                "Enrichment started",
                eventEnvelope.getEventId().toString()
        );

        // Enrichment placeholder (no mutation yet)
        DeliveryResult deliveryResult =
                routingClient.route(eventEnvelope);

        logger.info("Routing completed: {}", deliveryResult.getDestination());
        // Added: audit enrichment completion
        auditClient.audit(
                "ENRICHMENT",
                "COMPLETED",
                "Event enriched and forwarded to routing",
                eventEnvelope.getEventId().toString()
        );

        return eventEnvelope;
    }
}
