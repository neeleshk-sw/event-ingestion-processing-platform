package com.platform.intake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(scanBasePackages = { "com.platform.intake", "com.platform.common" })
@EnableScheduling
public class IntakeApplication {
    private static final Logger logger = LoggerFactory.getLogger(IntakeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IntakeApplication.class, args);
    }
}
