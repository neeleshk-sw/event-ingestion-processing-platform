package com.platform.intake.client;

import com.platform.common.contract.ValidationResult;
import com.platform.common.model.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ValidationClient {
    private static final Logger logger = LoggerFactory.getLogger(ValidationClient.class);

    private final RestClient restClient;

    public ValidationClient(RestClient.Builder builder,
            @Value("${service.validation.url:http://localhost:8082}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public ValidationResult validate(EventEnvelope eventEnvelope) {
        return restClient.post()
                .uri("/validate")
                .body(eventEnvelope)
                .retrieve()
                .body(ValidationResult.class);
    }

    public ValidationResult validate(EventEnvelope eventEnvelope, String correlationId) {

        logger.info("validate");

        return restClient.post()
                .uri("/validate")
                .header("X-Correlation-Id", correlationId)
                .body(eventEnvelope)
                .retrieve()
                .body(ValidationResult.class);
    }

}
