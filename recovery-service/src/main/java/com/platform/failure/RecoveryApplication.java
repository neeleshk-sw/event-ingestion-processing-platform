package com.platform.failure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.platform.failure", "com.platform.common" })
public class RecoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecoveryApplication.class, args);
    }
}
