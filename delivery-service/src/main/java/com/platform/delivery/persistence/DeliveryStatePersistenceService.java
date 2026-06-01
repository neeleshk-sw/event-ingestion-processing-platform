package com.platform.delivery.persistence;

import com.platform.common.model.EventEnvelope;
import com.platform.delivery.model.DeliveryState;
import com.platform.delivery.repository.DeliveryStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeliveryStatePersistenceService {

    private final DeliveryStateRepository deliveryStateRepository;

    public DeliveryStatePersistenceService(
            DeliveryStateRepository deliveryStateRepository) {
        this.deliveryStateRepository = deliveryStateRepository;
    }

    public String markInProgress(EventEnvelope eventEnvelope, String destination) {
        String idempotencyKey = buildIdempotencyKey(eventEnvelope, destination);

        DeliveryState state = deliveryStateRepository.findById(idempotencyKey)
                .orElseGet(DeliveryState::new);

        state.setIdempotencyKey(idempotencyKey);
        state.setEventId(eventEnvelope.getEventId());
        state.setDestination(destination);
        state.setStatus("IN_PROGRESS");
        state.setAttempts(state.getAttempts() + 1);
        state.setLastError(null);
        state.setUpdatedAt(Instant.now());

        if (eventEnvelope.getMetadata() != null) {
            state.setBatchId(eventEnvelope.getMetadata().getBatchId());
            state.setTraceId(eventEnvelope.getMetadata().getTraceId());
            state.setRetryCount(eventEnvelope.getMetadata().getRetryCount());
            state.setSource(eventEnvelope.getMetadata().getSource());
        }

        deliveryStateRepository.save(state);
        return idempotencyKey;
    }

    public void markDelivered(String idempotencyKey) {
        DeliveryState state = deliveryStateRepository.findById(idempotencyKey)
                .orElseThrow();

        state.setStatus("DELIVERED");
        state.setLastError(null);
        state.setUpdatedAt(Instant.now());
        deliveryStateRepository.save(state);
    }

    public void markFailed(String idempotencyKey, String errorMessage) {
        DeliveryState state = deliveryStateRepository.findById(idempotencyKey)
                .orElseThrow();

        state.setStatus("FAILED");
        state.setLastError(errorMessage);
        state.setUpdatedAt(Instant.now());
        deliveryStateRepository.save(state);
    }

    private String buildIdempotencyKey(EventEnvelope eventEnvelope, String destination) {
        return eventEnvelope.getEventId() + ":" + destination;
    }
}
