package com.craftistan.product;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.auth.dto.RegisterRequest;
import com.craftistan.product.dto.CreateProductRequest;
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
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String artisanToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register an artisan to get a token
        RegisterRequest register = new RegisterRequest();
        register.setName("Artisan User");
        register.setEmail("artisan@example.com");
        register.setPassword("password123");
        register.setRole(Role.ARTISAN);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        artisanToken = authResponse.getAccessToken();
    }

    @Test
    void shouldCreateProductAsArtisan() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Handmade Blue Pottery Vase");
        request.setPrice(new BigDecimal("2500.00"));
        request.setCategory("Pottery");
        request.setDescription("Authentic Multani blue pottery vase.");
        request.setStock(10);
        request.setImages(Collections.singletonList("http://example.com/image.jpg"));

        mockMvc.perform(post("/api/artisan/products")
                .header("Authorization", "Bearer " + artisanToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Handmade Blue Pottery Vase"))
                .andExpect(jsonPath("$.category").value("Pottery"));
    }

    @Test
    void shouldListProductsPublicly() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailCreateProductAsBuyer() throws Exception {
        // Register a buyer
        RegisterRequest register = new RegisterRequest();
        register.setName("Buyer User");
        register.setEmail("buyer@example.com");
        register.setPassword("password123");
        register.setRole(Role.BUYER);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthResponse.class);
        String buyerToken = authResponse.getAccessToken();

        CreateProductRequest request = new CreateProductRequest();
        request.setName("Illegal Product");
        request.setPrice(new BigDecimal("100"));
        request.setCategory("None");

        mockMvc.perform(post("/api/artisan/products")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
