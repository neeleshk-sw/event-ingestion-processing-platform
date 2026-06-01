package com.platform.normalization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = { "com.platform.normalization", "com.platform.common" })
public class NormalizationApplication {
    private static final Logger logger = LoggerFactory.getLogger(NormalizationApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NormalizationApplication.class, args);
    }
}
