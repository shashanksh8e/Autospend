package com.autospend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String categorizeTransaction(String merchantName,
                                        String contactName,
                                        String rawSms) {
        try {
            String prompt = buildPrompt(merchantName, contactName, rawSms);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(
                    mapper.createObjectNode()
                            .put("model", "llama-3.3-70b-versatile")
                            .put("max_tokens", 10)
                            .set("messages", mapper.createArrayNode()
                                    .add(mapper.createObjectNode()
                                            .put("role", "user")
                                            .put("content", prompt)))
            );

            String response = webClient.post()
                    .uri("/openai/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractCategory(response);

        } catch (WebClientResponseException e) {
            System.out.println("Groq response body: " + e.getResponseBodyAsString());
            return keywordFallback(merchantName, rawSms);
        } catch (Exception e) {
            System.out.println("Groq failed: " + e.getMessage());
            return keywordFallback(merchantName, rawSms);
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
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .trim();

            category = category.replaceAll("[^a-zA-Z]", "");
            System.out.println("Groq categorized as: " + category);
            return category;

        } catch (Exception e) {
            System.out.println("Error parsing Groq response: " + e.getMessage());
            return keywordFallback(null, null);
        }
    }

    private String keywordFallback(String merchantName, String rawSms) {
        String text = ((merchantName != null ? merchantName : "")
                + " " + (rawSms != null ? rawSms : "")).toLowerCase();

        if (text.matches(".*(zomato|swiggy|food|restaurant|cafe|pizza|burger|hotel).*"))
            return "Food";
        if (text.matches(".*(uber|ola|rapido|metro|bus|train|flight|travel|irctc).*"))
            return "Travel";
        if (text.matches(".*(amazon|flipkart|myntra|shopping|mall|store|shop).*"))
            return "Shopping";
        if (text.matches(".*(electricity|water|gas|bill|recharge|broadband|jio|airtel).*"))
            return "Bills";
        if (text.matches(".*(salary|credited|income|payroll).*"))
            return "Salary";
        if (text.matches(".*(hospital|pharmacy|doctor|medical|health|apollo|medplus).*"))
            return "Health";
        if (text.matches(".*(school|college|course|udemy|education|fees|tuition).*"))
            return "Education";
        if (text.matches(".*(netflix|spotify|prime|movie|cinema|entertainment).*"))
            return "Entertainment";
        if (text.matches(".*(grocer|bigbasket|blinkit|zepto|dmart|vegetables|fruits).*"))
            return "Groceries";
        if (text.matches(".*(rent|pg|hostel|landlord).*"))
            return "Rent";
        if (text.matches(".*(mutual fund|sip|stocks|zerodha|groww|investment).*"))
            return "Investments";

        return "Uncategorized";
    }
}