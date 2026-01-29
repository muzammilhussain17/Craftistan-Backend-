package com.craftistan.chat.controller;

import com.craftistan.chat.dto.ChatRequest;
import com.craftistan.chat.dto.ChatResponse;
import com.craftistan.chat.service.GeminiService;
import com.craftistan.common.dto.ApiResponse;
import com.craftistan.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI Chatbot endpoints with multilingual support")
public class ChatController {

    private final GeminiService geminiService;

    @PostMapping("/message")
    @Operation(summary = "Send a message to the chatbot", description = "Supports Roman Urdu, Punjabi, Pashto, Sindhi, Balochi, native scripts, and English")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @RequestBody ChatRequest request,
            @AuthenticationPrincipal User user) {

        String userId = user != null ? user.getId() : "guest";
        ChatResponse response = geminiService.chat(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "Clear chat session history")
    public ResponseEntity<ApiResponse<Void>> clearSession(@PathVariable String sessionId) {
        geminiService.clearSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Session cleared"));
    }

    @GetMapping("/health")
    @Operation(summary = "Check if chatbot is configured and working")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Chatbot is ready!", "OK"));
    }
}
