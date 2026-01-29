package com.craftistan.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String language; // Detected/used language
    private String sessionId;
    private LocalDateTime timestamp;
    private List<ProductSuggestion> productSuggestions; // Optional product recommendations

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSuggestion {
        private Long id;
        private String name;
        private String price;
        private String image;
    }
}
