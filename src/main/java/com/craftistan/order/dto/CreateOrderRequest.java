package com.craftistan.order.dto;

import com.craftistan.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    private ShippingAddressRequest shippingAddress;

    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }

    @Data
    public static class ShippingAddressRequest {
        private String fullName;
        private String phone;
        private String address;
        private String city;
        private String postalCode;
    }
}
