package com.platform.intake.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.common.model.EventEnvelope;
import com.platform.intake.model.EventReceipt;
import com.platform.intake.repository.EventReceiptRepository;
import com.platform.intake.service.EventIngestionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class EventReceiptPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(EventReceiptPersistenceService.class);

    private final EventReceiptRepository eventReceiptRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventReceiptPersistenceService(
            EventReceiptRepository eventReceiptRepository) {
        this.eventReceiptRepository = eventReceiptRepository;
    }

    public void saveReceipt(EventEnvelope eventEnvelope, String correlationId) {
        logger.info("saveReceipt:" + eventEnvelope.getEventType());
        EventReceipt receipt = new EventReceipt();
        receipt.setReceiptId(UUID.randomUUID());
        receipt.setEventId(eventEnvelope.getEventId());
        receipt.setProducerId(eventEnvelope.getProducerId());
        receipt.setCorrelationId(correlationId);
        receipt.setPayloadHash(calculatePayloadHash(eventEnvelope));
        receipt.setReceivedAt(Instant.now());

        if (eventEnvelope.getMetadata() != null) {
            receipt.setBatchId(eventEnvelope.getMetadata().getBatchId());
            receipt.setTraceId(eventEnvelope.getMetadata().getTraceId());
            receipt.setRetryCount(eventEnvelope.getMetadata().getRetryCount());
            receipt.setSource(eventEnvelope.getMetadata().getSource());
        }

        eventReceiptRepository.save(receipt);
    }

    private String calculatePayloadHash(EventEnvelope eventEnvelope) {
        Object payloadData = Map.of();
        if (eventEnvelope.getPayload() != null
                && eventEnvelope.getPayload().getData() != null) {
            payloadData = eventEnvelope.getPayload().getData();
        }

        try {
            String payloadJson = objectMapper.writeValueAsString(payloadData);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(payloadJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to compute payload hash", ex);
        }
    }
}
