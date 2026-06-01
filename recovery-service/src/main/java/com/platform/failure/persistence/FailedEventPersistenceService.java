package com.platform.failure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.failure.model.FailedEventEntity;
import com.platform.common.model.FailedEventRecord;
import com.platform.failure.repository.FailedEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FailedEventPersistenceService {

    private final FailedEventRepository failedEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FailedEventPersistenceService(
            FailedEventRepository failedEventRepository) {
        this.failedEventRepository = failedEventRepository;
    }

    public void saveFailure(FailedEventRecord record) {
        FailedEventEntity entity = new FailedEventEntity();
        entity.setFailureId(UUID.randomUUID());
        entity.setEventId(record.getEventId());
        entity.setStage(record.getStage());
        entity.setReason(record.getReason());
        entity.setPayload(toPayloadJson(record));
        entity.setCorrelationId(resolveCorrelationId(record));
        entity.setFailedAt(record.getFailedAt());

        if (record.getEvent() != null && record.getEvent().getMetadata() != null) {
            entity.setBatchId(record.getEvent().getMetadata().getBatchId());
            entity.setTraceId(record.getEvent().getMetadata().getTraceId());
            entity.setRetryCount(record.getEvent().getMetadata().getRetryCount());
            entity.setSource(record.getEvent().getMetadata().getSource());
        }

        failedEventRepository.save(entity);
    }

    private String toPayloadJson(FailedEventRecord record) {
        try {
            return objectMapper.writeValueAsString(record.getEvent());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize failed event payload", ex);
        }
    }

    private String resolveCorrelationId(FailedEventRecord record) {
        if (record.getEvent() == null || record.getEvent().getMetadata() == null) {
            return null;
        }
        return record.getEvent().getMetadata().getCorrelationId();
    }
}
