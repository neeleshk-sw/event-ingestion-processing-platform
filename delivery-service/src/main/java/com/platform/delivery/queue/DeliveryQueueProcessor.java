package com.platform.delivery.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.platform.common.model.EventEnvelope;
import com.platform.delivery.model.DeliveryQueueEntry;
import com.platform.delivery.repository.DeliveryQueueRepository;
import com.platform.delivery.service.EventDeliveryService;
import com.platform.common.util.MdcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeliveryQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryQueueProcessor.class);

    private final DeliveryQueueRepository deliveryQueueRepository;
    private final EventDeliveryService eventDeliveryService;
    private final ObjectMapper objectMapper;

    public DeliveryQueueProcessor(DeliveryQueueRepository deliveryQueueRepository,
            EventDeliveryService eventDeliveryService) {
        this.deliveryQueueRepository = deliveryQueueRepository;
        this.eventDeliveryService = eventDeliveryService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional
    public void processEntry(DeliveryQueueEntry entry) {
        try {
            deliveryQueueRepository.updateStatus(entry.getId(), "IN_PROGRESS");

            EventEnvelope envelope = objectMapper.readValue(entry.getPayload(), EventEnvelope.class);
            MdcUtil.syncMdc(envelope);
            eventDeliveryService.deliver(envelope);

            deliveryQueueRepository.updateStatus(entry.getId(), "DELIVERED");
            logger.info("Successfully processed queue entry: id={}, eventId={}", entry.getId(), entry.getEventId());

        } catch (Exception e) {
            // Graceful logging instead of filling up the logs with stack traces
            logger.error("Failed to process queue entry (id={}, eventId={}): {}",
                    entry.getId(), entry.getEventId(), e.getMessage());

            // Mark as failed in its own transaction (if possible) or at least update the
            // status
            try {
                deliveryQueueRepository.updateStatus(entry.getId(), "FAILED");
            } catch (Exception updateEx) {
                logger.error("Critical: Could not update status to FAILED for entry {}: {}",
                        entry.getId(), updateEx.getMessage());
            }
        } finally {
            MDC.clear();
        }
    }
}
