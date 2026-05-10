package com.aigc.intelliengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.aigc.intelliengine")
@EnableCaching
public class IntelliEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntelliEngineApplication.class, args);
    }
}
