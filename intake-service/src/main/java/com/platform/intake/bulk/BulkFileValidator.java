package com.platform.intake.bulk;

import com.platform.common.exception.InvalidEventException;
import com.platform.common.model.EventEnvelope;
import com.platform.common.model.EventMetadata;
import com.platform.common.model.EventPayload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BulkFileValidator {

    public void validate(List<EventEnvelope> events) {
        if (events == null || events.isEmpty()) {
            throw new InvalidEventException("Bulk file contains no events");
        }

        for (int i = 0; i < events.size(); i++) {
            EventEnvelope event = events.get(i);
            validateEvent(event, i);
        }
    }

    private void validateEvent(EventEnvelope event, int index) {
        if (event == null) {
            throw new InvalidEventException("Event at index " + index + " is null");
        }
        if (event.getEventId() == null) {
            throw new InvalidEventException("Event at index " + index + " missing eventId");
        }
        if (isBlank(event.getProducerId())) {
            throw new InvalidEventException("Event at index " + index + " missing producerId");
        }
        if (isBlank(event.getEventType())) {
            throw new InvalidEventException("Event at index " + index + " missing eventType");
        }

        EventMetadata metadata = event.getMetadata();
        if (metadata == null || isBlank(metadata.getCorrelationId())) {
            throw new InvalidEventException("Event at index " + index + " missing metadata.correlationId");
        }

        EventPayload payload = event.getPayload();
        if (payload == null) {
            throw new InvalidEventException("Event at index " + index + " missing payload");
        }
        Map<String, Object> data = payload.getData();
        if (data == null) {
            throw new InvalidEventException("Event at index " + index + " missing payload.data");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
