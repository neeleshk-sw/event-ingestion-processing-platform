package com.platform.delivery.client;

import com.platform.common.model.FailedEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FailureRecoveryClient {

    private static final Logger logger = LoggerFactory.getLogger(FailureRecoveryClient.class);

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    private final RestClient restClient;

    public FailureRecoveryClient(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8087")
                .build();
    }

    public void recordFailure(FailedEventRecord record) {

        String correlationId = MDC.get(MDC_KEY);

        logger.info("Reporting failure to Failure & Recovery service");

        restClient.post()
                .uri("/failures")
                .header(CORRELATION_HEADER, correlationId)
                .body(record)
                .retrieve()
                .toBodilessEntity();
    }
}
