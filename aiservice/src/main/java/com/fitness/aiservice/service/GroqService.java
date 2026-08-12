package com.fitness.aiservice.service;

import com.fitness.aiservice.model.GroqChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Configuration
class WebClientConfig {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Bean
    public WebClient groqWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

@RequiredArgsConstructor
@Service
public class GroqService {

    private final WebClient groqWebClient;

    public String getChatAnswer(String question) {

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", question
                        )
                )
        );

        GroqChatResponse response = groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GroqChatResponse.class)
                .block();

        return response.getChoices()
                .get(0)
                .getMessage()
                .getContent(); // ✅ PURE JSON FROM AI
    }
}
