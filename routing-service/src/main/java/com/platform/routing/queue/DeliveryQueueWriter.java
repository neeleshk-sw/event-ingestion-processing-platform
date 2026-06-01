package com.platform.routing.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class DeliveryQueueWriter {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryQueueWriter.class);

    private final DeliveryQueueRepository deliveryQueueRepository;
    private final ObjectMapper objectMapper;

    public DeliveryQueueWriter(DeliveryQueueRepository deliveryQueueRepository) {
        this.deliveryQueueRepository = deliveryQueueRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void enqueue(EventEnvelope eventEnvelope, String destination,
            String routingKey, int priority) {

        DeliveryQueueEntry entry = new DeliveryQueueEntry();
        entry.setId(UUID.randomUUID());
        entry.setEventId(eventEnvelope.getEventId());
        entry.setPayload(serialize(eventEnvelope));
        entry.setDestination(destination);
        entry.setRoutingKey(routingKey);
        entry.setPriority(priority);
        entry.setStatus("NEW");
        entry.setQueuedAt(Instant.now());

        if (eventEnvelope.getMetadata() != null) {
            entry.setBatchId(eventEnvelope.getMetadata().getBatchId());
            entry.setTraceId(eventEnvelope.getMetadata().getTraceId());
            entry.setRetryCount(eventEnvelope.getMetadata().getRetryCount());
            entry.setSource(eventEnvelope.getMetadata().getSource());
        }

        deliveryQueueRepository.save(entry);

        logger.info("Enqueued delivery intent: eventId={}, destination={}, routingKey={}",
                eventEnvelope.getEventId(), destination, routingKey);
    }

    private String serialize(EventEnvelope eventEnvelope) {
        try {
            return objectMapper.writeValueAsString(eventEnvelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize EventEnvelope for queue", e);
        }
    }
}
