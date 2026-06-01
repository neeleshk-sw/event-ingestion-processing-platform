package com.platform.routing.client;

import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DeliveryControlClient {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryControlClient.class);

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private final RestClient restClient;

    public DeliveryControlClient(RestClient.Builder builder,
            @Value("${service.delivery.url:http://localhost:8086}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public void deliver(EventEnvelope eventEnvelope) {

        String correlationId = MDC.get(MDC_KEY);

        logger.info("Calling Delivery Control Service");

        restClient.post()
                .uri("/deliver")
                .header(CORRELATION_HEADER, correlationId)
                .body(eventEnvelope)
                .retrieve()
                .toBodilessEntity();
    }
}
