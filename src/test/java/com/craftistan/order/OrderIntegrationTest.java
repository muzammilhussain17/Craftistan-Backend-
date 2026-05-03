package com.craftistan.order;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.auth.dto.RegisterRequest;
import com.craftistan.order.dto.CreateOrderRequest;
import com.craftistan.product.dto.CreateProductRequest;
import com.craftistan.product.dto.ProductDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class OrderIntegrationTest {

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
        artReg.setEmail("art@ex.com");
        artReg.setPassword("pass123");
        artReg.setRole(Role.ARTISAN);

        MvcResult artRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(artReg)))
                .andReturn();
        String artToken = objectMapper.readValue(artRes.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

        CreateProductRequest prodReq = new CreateProductRequest();
        prodReq.setName("Test Product");
        prodReq.setPrice(new BigDecimal("1000"));
        prodReq.setCategory("Test");
        prodReq.setStock(50);

        MvcResult prodRes = mockMvc.perform(post("/api/artisan/products")
                .header("Authorization", "Bearer " + artToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(prodReq)))
                .andReturn();
        productId = objectMapper.readValue(prodRes.getResponse().getContentAsString(), ProductDto.class).getId();

        // 2. Register Buyer
        RegisterRequest buyReg = new RegisterRequest();
        buyReg.setName("Buyer");
        buyReg.setEmail("buy@ex.com");
        buyReg.setPassword("pass123");
        buyReg.setRole(Role.BUYER);

        MvcResult buyRes = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buyReg)))
                .andReturn();
        buyerToken = objectMapper.readValue(buyRes.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();
    }

    @Test
    void shouldCreateOrderAsBuyer() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        
        CreateOrderRequest.OrderItemRequest item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(2);
        request.setItems(Collections.singletonList(item));

        CreateOrderRequest.ShippingAddressRequest addr = new CreateOrderRequest.ShippingAddressRequest();
        addr.setFullName("Test Buyer");
        addr.setPhone("03001234567");
        addr.setAddress("Street 1, House 2");
        addr.setCity("Lahore");
        addr.setPostalCode("54000");
        request.setShippingAddress(addr);

        mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
