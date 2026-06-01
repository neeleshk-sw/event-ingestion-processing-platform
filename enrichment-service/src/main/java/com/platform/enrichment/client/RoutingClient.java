package com.platform.enrichment.client;

import com.platform.common.contract.DeliveryResult;
import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RoutingClient {

    private static final Logger logger = LoggerFactory.getLogger(RoutingClient.class);

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private final RestClient restClient;

    public RoutingClient(RestClient.Builder builder,
            @Value("${service.routing.url:http://localhost:8085}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public DeliveryResult route(EventEnvelope eventEnvelope) {

        String correlationId = MDC.get(MDC_KEY);

        logger.info("Calling Routing Service");

        return restClient.post()
                .uri("/route")
                .header(CORRELATION_HEADER, correlationId)
                .body(eventEnvelope)
                .retrieve()
                .body(DeliveryResult.class);
    }
}
