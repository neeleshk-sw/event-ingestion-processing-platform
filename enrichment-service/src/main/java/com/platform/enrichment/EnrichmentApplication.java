package com.platform.enrichment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.platform.enrichment", "com.platform.common" })
public class EnrichmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnrichmentApplication.class, args);
    }
}
