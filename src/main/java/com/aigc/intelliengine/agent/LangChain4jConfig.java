package com.aigc.intelliengine.agent;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j")
@ConditionalOnProperty(name = "langchain4j.enabled", havingValue = "true")
public class LangChain4jConfig {

    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String modelName = "gpt-4o-mini";
    private double temperature = 0.7;
    private int maxTokens = 512;
    private int maxMemoryMessages = 10;

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(maxMemoryMessages);
    }

    @Bean
    public LangChain4jAgent langChain4jAgent(OpenAiChatModel model, ChatMemory memory, LangChain4jTools tools) {
        return AiServices.builder(LangChain4jAgent.class)
                .chatLanguageModel(model)
                .chatMemory(memory)
                .tools(tools)
                .build();
    }
}
