package com.kkvat.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class KkvatAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkvatAutomationApplication.class, args);
    }
}
