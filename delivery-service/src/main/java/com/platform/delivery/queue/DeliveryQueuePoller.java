package com.platform.delivery.queue;

import com.platform.delivery.model.DeliveryQueueEntry;
import com.platform.delivery.repository.DeliveryQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryQueuePoller {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryQueuePoller.class);

    private final DeliveryQueueRepository deliveryQueueRepository;
    private final DeliveryQueueProcessor deliveryQueueProcessor;

    public DeliveryQueuePoller(DeliveryQueueRepository deliveryQueueRepository,
            DeliveryQueueProcessor deliveryQueueProcessor) {
        this.deliveryQueueRepository = deliveryQueueRepository;
        this.deliveryQueueProcessor = deliveryQueueProcessor;
    }

    @Scheduled(fixedDelayString = "${delivery.queue.poll-interval-ms:5000}")
    public void poll() {
        try {
            List<DeliveryQueueEntry> entries = deliveryQueueRepository.findTop10ByStatusOrderByQueuedAtAsc("NEW");

            if (entries.isEmpty()) {
                return;
            }

            logger.info("Delivery queue poller: found {} NEW entries", entries.size());

            for (DeliveryQueueEntry entry : entries) {
                deliveryQueueProcessor.processEntry(entry);
            }
        } catch (Exception e) {
            logger.error("Unexpected error in delivery queue poller: {}", e.getMessage());
        }
    }
}
