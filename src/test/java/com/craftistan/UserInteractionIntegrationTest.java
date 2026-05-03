package com.craftistan;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.auth.dto.RegisterRequest;
import com.craftistan.product.dto.CreateProductRequest;
import com.craftistan.product.dto.ProductDto;
import com.craftistan.review.dto.CreateReviewRequest;
import com.craftistan.user.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class UserInteractionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String buyerToken;
    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Register Artisan and Create a Product
        RegisterRequest artReg = new RegisterRequest();
        artReg.setName("Artisan");
        artReg.setEmail("artisan_interact@ex.com");
        artReg.setPassword("pass123");
        artReg.setRole(Role.ARTISAN);

        MvcResult artRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artReg)))
                .andReturn();
        String artToken = objectMapper.readValue(artRes.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        CreateProductRequest prodReq = new CreateProductRequest();
        prodReq.setName("Interaction Product");
        prodReq.setPrice(new BigDecimal("500"));
        prodReq.setCategory("Decor");
        prodReq.setStock(10);

        MvcResult prodRes = mockMvc.perform(post("/api/artisan/products")
                .header("Authorization", "Bearer " + artToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prodReq)))
                .andReturn();
        productId = objectMapper.readValue(prodRes.getResponse().getContentAsString(), ProductDto.class).getId();

        // 2. Register Buyer
        RegisterRequest buyReg = new RegisterRequest();
        buyReg.setName("Buyer");
        buyReg.setEmail("buyer_interact@ex.com");
        buyReg.setPassword("pass123");
        buyReg.setRole(Role.BUYER);

        MvcResult buyRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyReg)))
                .andReturn();
        buyerToken = objectMapper.readValue(buyRes.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();
    }

    @Test
    void shouldManageWishlist() throws Exception {
        // Add to wishlist
        mockMvc.perform(post("/api/wishlist/" + productId)
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Added to wishlist"));

        // Check wishlist
        mockMvc.perform(get("/api/wishlist")
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(productId));

        // Remove from wishlist
        mockMvc.perform(delete("/api/wishlist/" + productId)
                .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Removed from wishlist"));
    }

    @Test
    void shouldCreateAndManageReview() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("Excellent quality!");

        // Create review
        mockMvc.perform(post("/api/products/" + productId + "/reviews")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Excellent quality!"));

        // Get reviews
        mockMvc.perform(get("/api/products/" + productId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].comment").value("Excellent quality!"));
    }
}
