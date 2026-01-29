package com.craftistan.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for translated product content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslatedContent {
    private String name;
    private String description;
}
