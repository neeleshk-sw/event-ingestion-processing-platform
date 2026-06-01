package com.platform.normalization.client;

import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EnrichmentClient {

    private static final Logger logger = LoggerFactory.getLogger(EnrichmentClient.class);

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private final RestClient restClient;

    public EnrichmentClient(RestClient.Builder builder,
            @Value("${service.enrichment.url:http://localhost:8084}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public EventEnvelope enrich(EventEnvelope eventEnvelope) {

        String correlationId = MDC.get(MDC_KEY);

        logger.info("Calling Enrichment Service");

        return restClient.post()
                .uri("/enrich")
                .header(CORRELATION_HEADER, correlationId)
                .body(eventEnvelope)
                .retrieve()
                .body(EventEnvelope.class);
    }
}
