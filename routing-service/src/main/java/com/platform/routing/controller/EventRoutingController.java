package com.platform.routing.controller;

import com.platform.common.contract.DeliveryResult;
import com.platform.common.model.EventEnvelope;
import com.platform.routing.service.EventRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/route")
public class EventRoutingController {

    private static final Logger logger =
            LoggerFactory.getLogger(EventRoutingController.class);

    private final EventRoutingService routingService;

    public EventRoutingController(EventRoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping
    public ResponseEntity<DeliveryResult> route(
            @RequestBody EventEnvelope eventEnvelope) {

        logger.info("Routing event");
        DeliveryResult result = routingService.route(eventEnvelope);
        return ResponseEntity.ok(result);
    }
}
