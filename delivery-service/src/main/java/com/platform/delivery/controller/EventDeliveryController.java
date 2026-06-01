package com.platform.delivery.controller;

import com.platform.common.model.EventEnvelope;
import com.platform.delivery.service.EventDeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliver")
public class EventDeliveryController {

    private static final Logger logger =
            LoggerFactory.getLogger(EventDeliveryController.class);

    private final EventDeliveryService deliveryService;

    public EventDeliveryController(EventDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<String> deliver(
            @RequestBody EventEnvelope eventEnvelope) {

        logger.info("Received delivery request");
        deliveryService.deliver(eventEnvelope);
        return ResponseEntity.ok("DELIVERY_ACCEPTED");
    }
}
