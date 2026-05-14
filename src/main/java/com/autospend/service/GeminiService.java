package com.autospend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String categorizeTransaction(String merchantName,
                                        String contactName,
                                        String rawSms) {
        try {
            String prompt = buildPrompt(merchantName, contactName, rawSms);

            String requestBody = """
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"
                        }]
                    }]
                }
                """.formatted(prompt);

            String response = webClient.post()
                    .uri("/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractCategory(response);

        } catch (Exception e) {
            System.out.println("Gemini API error: " + e.getMessage());
            return "Uncategorized";
        }
    }

    private String buildPrompt(String merchantName,
                               String contactName,
                               String rawSms) {
        return ("Categorize this UPI transaction into exactly ONE of these categories: " +
                "Food, Travel, Shopping, Bills, Friends, Investments, Health, " +
                "Education, Entertainment, Groceries, Rent, Salary, Uncategorized. " +
                "Merchant: " + merchantName + ". " +
                "Contact: " + contactName + ". " +
                "SMS: " + rawSms + ". " +
                "Reply with ONLY the category name, nothing else.")
                .replace("\"", "'");
    }

    private String extractCategory(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String category = root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText()
                    .trim();

            // Remove any punctuation
            category = category.replaceAll("[^a-zA-Z]", "");

            System.out.println("Gemini categorized as: " + category);
            return category;

        } catch (Exception e) {
            System.out.println("Error parsing Gemini response: " + e.getMessage());
            return "Uncategorized";
        }
    }
}