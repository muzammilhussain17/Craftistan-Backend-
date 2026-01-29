package com.craftistan.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    private String style;

    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> images;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;
}
