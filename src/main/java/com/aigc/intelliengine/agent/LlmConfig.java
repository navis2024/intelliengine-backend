package com.aigc.intelliengine.agent;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {
    private boolean enabled = false;
    private String provider = "openai";
    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private double temperature = 0.7;
    private int maxTokens = 512;
}
