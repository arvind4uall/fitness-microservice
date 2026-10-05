package com.fsdarvind.fitness.aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;

    GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of("contents", new Object[]{
                Map.of("parts", new Object[]{
                        Map.of("text", question)
                })
        });

        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("X-goog-api-key", geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body -> new GeminiApiException(
                                        "Gemini call failed with " + clientResponse.statusCode() + ": " + body)))
                .bodyToMono(String.class)
                .block();
        return response;
    }

    /** Carries the Gemini status and error body without dragging in a reactor stack trace. */
    public static class GeminiApiException extends RuntimeException {
        public GeminiApiException(String message) {
            super(message);
        }
    }
}
