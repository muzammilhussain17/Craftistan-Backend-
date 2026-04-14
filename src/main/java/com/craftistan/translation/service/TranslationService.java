package com.craftistan.translation.service;

import com.craftistan.product.entity.Product;
import com.craftistan.product.repository.ProductRepository;
import com.craftistan.translation.dto.TranslatedContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for translating product content using Google Gemini API
 */
@Service
@Slf4j
public class TranslationService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String model;

    // Supported languages for translation
    public static final List<String> SUPPORTED_LANGUAGES = List.of("en", "ur", "pa", "sd", "ps", "bal");

    private static final Map<String, String> LANGUAGE_NAMES = Map.of(
            "en", "English",
            "ur", "Urdu",
            "pa", "Punjabi",
            "sd", "Sindhi",
            "ps", "Pashto",
            "bal", "Balochi");

    public TranslationService(ObjectMapper objectMapper, ProductRepository productRepository) {
        this.objectMapper = objectMapper;
        this.productRepository = productRepository;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    /**
     * Asynchronously translate product to all supported languages
     */
    @Async
    public void translateProductAsync(Product product) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key not configured, skipping translation");
            return;
        }

        try {
            log.info("Starting translation for product ID: {}", product.getId());

            String sourceLanguage = product.getOriginalLanguage() != null
                    ? product.getOriginalLanguage()
                    : "en";

            Map<String, TranslatedContent> translations = translateToAllLanguages(
                    product.getName(),
                    product.getDescription(),
                    sourceLanguage);

            // Store original in translations map too
            translations.put(sourceLanguage, TranslatedContent.builder()
                    .name(product.getName())
                    .description(product.getDescription())
                    .build());

            // Convert to JSON and save
            String translationsJson = objectMapper.writeValueAsString(translations);
            product.setTranslations(translationsJson);
            productRepository.save(product);

            log.info("Translation completed for product ID: {}. Languages: {}",
                    product.getId(), translations.keySet());

        } catch (Exception e) {
            log.error("Error translating product ID: {}", product.getId(), e);
        }
    }

    /**
     * Translate content to all supported languages except source
     */
    public Map<String, TranslatedContent> translateToAllLanguages(
            String name, String description, String sourceLanguage) {

        Map<String, TranslatedContent> translations = new HashMap<>();

        for (String targetLang : SUPPORTED_LANGUAGES) {
            if (targetLang.equals(sourceLanguage)) {
                continue; // Skip source language
            }

            try {
                TranslatedContent translated = translateContent(name, description, sourceLanguage, targetLang);
                if (translated != null) {
                    translations.put(targetLang, translated);
                }
            } catch (Exception e) {
                log.error("Error translating to {}: {}", targetLang, e.getMessage());
            }
        }

        return translations;
    }

    /**
     * Translate content to a specific language using Gemini API
     */
    private TranslatedContent translateContent(
            String name, String description, String sourceLang, String targetLang) {

        String sourceName = LANGUAGE_NAMES.getOrDefault(sourceLang, "English");
        String targetName = LANGUAGE_NAMES.getOrDefault(targetLang, targetLang);

        String prompt = String.format("""
                You are a professional translator specializing in Pakistani languages.
                Translate the following product information from %s to %s.

                IMPORTANT: Respond ONLY with a valid JSON object in this exact format, no other text:
                {"name": "translated product name", "description": "translated product description"}

                Product Name: %s
                Product Description: %s
                """, sourceName, targetName, name, description != null ? description : "");

        try {
            // Build request body
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "maxOutputTokens", 500,
                            "temperature", 0.3));

            // Call Gemini API
            String responseBody = webClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content").path("parts");
                if (content.isArray() && content.size() > 0) {
                    String jsonText = content.get(0).path("text").asText();
                    // Use Regex to aggressively extract the JSON object in case of markdown wrapping
                    Matcher matcher = Pattern.compile("\\{.*\\}", Pattern.DOTALL).matcher(jsonText);
                    if (matcher.find()) {
                        jsonText = matcher.group(0);
                    } else {
                        log.warn("Could not find a JSON object in Gemini response for {}", targetLang);
                        return null;
                    }

                    // Parse the JSON response
                    JsonNode translationNode = objectMapper.readTree(jsonText);
                    return TranslatedContent.builder()
                            .name(translationNode.path("name").asText())
                            .description(translationNode.path("description").asText())
                            .build();
                }
            }

            log.warn("Unexpected Gemini response format for {} translation", targetLang);
            return null;

        } catch (Exception e) {
            log.error("Error calling Gemini API for {} translation: {}", targetLang, e.getMessage());
            return null;
        }
    }

    /**
     * Get translation for specific language from stored JSON
     */
    public TranslatedContent getTranslation(String translationsJson, String language) {
        if (translationsJson == null || translationsJson.isEmpty()) {
            return null;
        }

        try {
            Map<String, TranslatedContent> translations = objectMapper.readValue(
                    translationsJson,
                    new TypeReference<Map<String, TranslatedContent>>() {
                    });
            return translations.get(language);
        } catch (JsonProcessingException e) {
            log.error("Error parsing translations JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if language is supported
     */
    public boolean isLanguageSupported(String language) {
        return SUPPORTED_LANGUAGES.contains(language);
    }
}
