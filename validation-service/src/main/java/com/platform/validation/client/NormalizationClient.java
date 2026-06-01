package com.platform.validation.client;

import com.platform.common.contract.NormalizationResult;
import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NormalizationClient {
    private static final Logger logger = LoggerFactory.getLogger(NormalizationClient.class);

    private final RestClient restClient;

    public NormalizationClient(RestClient.Builder builder,
            @Value("${service.normalization.url:http://localhost:8083}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public NormalizationResult normalize(EventEnvelope eventEnvelope) {
        return restClient.post()
                .uri("/normalize")
                .body(eventEnvelope)
                .retrieve()
                .body(NormalizationResult.class);
    }

    public NormalizationResult normalize(EventEnvelope eventEnvelope, String correlationId) {

        return restClient.post()
                .uri("/normalize")
                .header("X-Correlation-Id", correlationId)
                .body(eventEnvelope)
                .retrieve()
                .body(NormalizationResult.class);
    }

}
