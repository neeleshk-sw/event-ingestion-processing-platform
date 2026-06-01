package com.platform.routing.service;

import com.platform.common.contract.DeliveryResult;
import com.platform.common.model.EventEnvelope;
import com.platform.common.model.EventMetadata;
import com.platform.routing.client.AuditClient;
import com.platform.routing.config.RabbitRoutingConfig;
import com.platform.routing.config.RoutingProperties;
import com.platform.routing.config.RoutingRule;
import com.platform.routing.queue.DeliveryQueueWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EventRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(EventRoutingService.class);

    private final AuditClient auditClient;
    private final RoutingProperties routingProperties;
    private final DeliveryQueueWriter deliveryQueueWriter;

    public EventRoutingService(AuditClient auditClient,
            RoutingProperties routingProperties,
            DeliveryQueueWriter deliveryQueueWriter) {
        this.auditClient = auditClient;
        this.routingProperties = routingProperties;
        this.deliveryQueueWriter = deliveryQueueWriter;
    }

    public DeliveryResult route(EventEnvelope eventEnvelope) {

        logger.info("Applying routing rules");
        auditClient.audit(
                "ROUTING",
                "STARTED",
                "Routing evaluation started",
                eventEnvelope.getEventId().toString());

        for (RoutingRule rule : routingProperties.getRules()) {
            if (matches(rule.getWhen(), eventEnvelope)) {
                logger.info("Routing rule matched: {}", rule.getName());
                auditClient.audit(
                        "ROUTING",
                        "ROUTED",
                        "Matched rule " + rule.getName()
                                + ", destination=" + rule.getDestination(),
                        eventEnvelope.getEventId().toString());

                enqueueForDelivery(eventEnvelope, rule.getDestination());
                return new DeliveryResult(true, rule.getDestination());
            }
        }

        logger.warn("No routing rule matched, using default");
        auditClient.audit(
                "ROUTING",
                "DEFAULT_ROUTE",
                "No explicit rule matched, using default destination",
                eventEnvelope.getEventId().toString());

        enqueueForDelivery(eventEnvelope, "DEFAULT_DESTINATION");
        return new DeliveryResult(true, "DEFAULT_DESTINATION");
    }

    private boolean matches(Map<String, String> conditions, EventEnvelope event) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String expected = entry.getValue();
            String actual = extractValue(entry.getKey(), event);
            if (!expected.equalsIgnoreCase(actual)) {
                return false;
            }
        }
        return true;
    }

    private String extractValue(String key, EventEnvelope event) {
        return switch (key) {
            case "producerId" -> event.getProducerId();
            case "priority" ->
                event.getMetadata()
                        .getAttributes()
                        .get("priority");
            default -> null;
        };
    }

    private void enqueueForDelivery(EventEnvelope eventEnvelope, String destination) {
        if (eventEnvelope.getMetadata() == null) {
            eventEnvelope.setMetadata(new EventMetadata());
        }
        if (eventEnvelope.getMetadata().getAttributes() == null) {
            eventEnvelope.getMetadata().setAttributes(new HashMap<>());
        }
        eventEnvelope.getMetadata().getAttributes().put("destination", destination);

        String producerId = eventEnvelope.getProducerId() == null
                ? ""
                : eventEnvelope.getProducerId().toLowerCase();

        String routingKey;
        int priority;

        if (producerId.contains("mobile")) {
            routingKey = RabbitRoutingConfig.KEY_MOBILE;
            priority = 10;
        } else if (producerId.contains("api")) {
            routingKey = RabbitRoutingConfig.KEY_API;
            priority = 5;
        } else {
            routingKey = RabbitRoutingConfig.KEY_DEFAULT;
            priority = 1;
        }

        // DB commit is the final handoff — no availability check, no retry
        deliveryQueueWriter.enqueue(eventEnvelope, destination, routingKey, priority);
    }
}
