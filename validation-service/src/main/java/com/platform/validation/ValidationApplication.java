package com.platform.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = { "com.platform.validation", "com.platform.common" })
public class ValidationApplication {
    private static final Logger logger = LoggerFactory.getLogger(ValidationApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ValidationApplication.class, args);
    }
}
