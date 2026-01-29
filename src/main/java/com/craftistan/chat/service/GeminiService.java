package com.craftistan.chat.service;

import com.craftistan.chat.dto.ChatRequest;
import com.craftistan.chat.dto.ChatResponse;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.product.service.ProductService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${app.gemini.max-tokens:1024}")
    private int maxTokens;

    // Simple session storage for conversation context (in production, use Redis or
    // DB)
    private final Map<String, List<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            You are Craftistan Assistant, a helpful and friendly AI for a Pakistani artisan marketplace
            that sells handcrafted products like textiles, pottery, jewelry, and home decor.

            SUPPORTED LANGUAGES & SCRIPTS:
            - Roman Urdu/Hindi (e.g., "mujhay kapry dekhao", "yeh kitne ka hai", "mera order kahan hai")
            - Roman Punjabi (e.g., "mein hath da kam labh raha haan")
            - Roman Pashto (e.g., "za da lasi kar ghwaram")
            - Roman Sindhi, Roman Balochi
            - Native scripts: اردو، پنجابی، پښتو، سنڌي، بلوچی
            - English

            CAPABILITIES:
            1. Product Discovery - Help customers find handcrafted products (textiles, pottery, jewelry, home decor)
            2. Order Tracking - Help with order status queries (ask them to check "My Orders" page)
            3. Artisan Help - Guide artisans on how to list products on the platform
            4. General FAQs - Answer questions about Craftistan, shipping, payments, returns

            PRODUCT CATEGORIES:
            - Textiles (kapry, fabrics, ajrak, embroidery)
            - Pottery (mitti ke bartan, ceramics)
            - Jewelry (zewar, traditional jewelry)
            - Home Decor (ghar ki sajawat)

            RULES:
            - Understand Roman Urdu/transliterated text (this is most common in Pakistan)
            - Respond in the SAME style the user writes (if they write Roman Urdu, respond in Roman Urdu)
            - Be culturally aware, warm, and respectful
            - Use greetings like "Assalam o Alaikum" when appropriate
            - Keep responses concise, friendly, and helpful (2-3 sentences max)
            - If user asks about specific products, mention you can show them options
            - For order issues, guide them to the "My Orders" section or customer support
            - Never make up product prices or availability - suggest they browse the shop

            CONTEXT: The user is browsing Craftistan, an e-commerce platform for Pakistani artisan crafts.
            """;

    public GeminiService(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public ChatResponse chat(ChatRequest request, String userId) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Gemini API key not configured");
            return ChatResponse.builder()
                    .message("Chatbot is not configured. Please set GEMINI_API_KEY environment variable.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        String userMessage = request.getMessage();

        log.info("Chat request - sessionId: {}, message: {}", sessionId, userMessage);

        try {
            // Get conversation history
            List<Map<String, String>> history = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

            // Check if user is asking about products
            List<ChatResponse.ProductSuggestion> suggestions = null;
            String productContext = "";
            if (isProductQuery(userMessage)) {
                suggestions = getProductSuggestions(userMessage);
                if (!suggestions.isEmpty()) {
                    productContext = "\n\nAvailable products matching the query: " +
                            suggestions.stream()
                                    .map(p -> p.getName() + " (Rs. " + p.getPrice() + ")")
                                    .collect(Collectors.joining(", "));
                }
            }

            // Build the request to Gemini
            String response = callGeminiApi(userMessage, history, productContext);

            // Update conversation history
            history.add(Map.of("role", "user", "content", userMessage));
            history.add(Map.of("role", "assistant", "content", response));

            // Keep only last 10 exchanges
            if (history.size() > 20) {
                history.subList(0, history.size() - 20).clear();
            }

            return ChatResponse.builder()
                    .message(response)
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .productSuggestions(suggestions)
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return ChatResponse.builder()
                    .message(
                            "Maaf kijiye, abhi kuch masla hai. Thori der baad dobara try karein. (Sorry, there's an issue. Please try again later.)")
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private String callGeminiApi(String userMessage, List<Map<String, String>> history, String productContext) {
        // Build conversation content for Gemini
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add system instruction
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", SYSTEM_PROMPT + productContext))));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text",
                        "Assalam o Alaikum! Main Craftistan ka assistant hoon. Aap kaise madad kar sakta hoon? (Hello! I'm Craftistan's assistant. How can I help you?)"))));

        // Add conversation history
        for (Map<String, String> msg : history) {
            String role = "user".equals(msg.get("role")) ? "user" : "model";
            contents.add(Map.of(
                    "role", role,
                    "parts", List.of(Map.of("text", msg.get("content")))));
        }

        // Add current user message
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))));

        // Build request body
        Map<String, Object> requestBody = Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "maxOutputTokens", maxTokens,
                        "temperature", 0.7));

        // Call Gemini API
        String responseBody = webClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse response
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content").path("parts");
                if (content.isArray() && content.size() > 0) {
                    return content.get(0).path("text").asText();
                }
            }
            log.warn("Unexpected Gemini response format: {}", responseBody);
            return "Maaf kijiye, jawab samajh nahi aaya. (Sorry, couldn't understand the response.)";
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", responseBody, e);
            return "Maaf kijiye, kuch masla hua. (Sorry, something went wrong.)";
        }
    }

    private boolean isProductQuery(String message) {
        String lower = message.toLowerCase();
        // Keywords for product queries (Roman Urdu + English)
        List<String> productKeywords = List.of(
                "dikha", "show", "find", "search", "product", "item",
                "kapry", "kapra", "cloth", "textile", "fabric", "ajrak",
                "bartan", "pottery", "ceramic", "mitti",
                "zewar", "jewelry", "jewellery",
                "ghar", "home", "decor", "decoration",
                "kharid", "buy", "purchase", "price", "kitne", "cost",
                "embroidery", "handmade", "craft");
        return productKeywords.stream().anyMatch(lower::contains);
    }

    private List<ChatResponse.ProductSuggestion> getProductSuggestions(String query) {
        try {
            // Get some products based on query
            var products = productService.getAllProducts(PageRequest.of(0, 4));
            return products.getContent().stream()
                    .map(p -> ChatResponse.ProductSuggestion.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .price(p.getPrice().toString())
                            .image(p.getImage())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching product suggestions", e);
            return List.of();
        }
    }

    public void clearSession(String sessionId) {
        conversationHistory.remove(sessionId);
    }
}
