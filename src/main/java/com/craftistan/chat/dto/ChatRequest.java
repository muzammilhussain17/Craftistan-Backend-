package com.craftistan.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String language; // Optional: user's preferred language (e.g., "ur", "en", "pa")
    private String sessionId; // Optional: to maintain conversation context
}
