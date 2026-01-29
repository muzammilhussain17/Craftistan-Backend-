package com.craftistan.user.controller;

import com.craftistan.common.dto.ApiResponse;
import com.craftistan.user.dto.AddressDto;
import com.craftistan.user.entity.User;
import com.craftistan.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "User address management")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Get user's addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses(
            @AuthenticationPrincipal User user) {
        List<AddressDto> addresses = addressService.getUserAddresses(user.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PostMapping
    @Operation(summary = "Add new address")
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(
            @Valid @RequestBody AddressDto request,
            @AuthenticationPrincipal User user) {
        AddressDto address = addressService.createAddress(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(address, "Address added successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDto request,
            @AuthenticationPrincipal User user) {
        AddressDto address = addressService.updateAddress(id, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(address, "Address updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        addressService.deleteAddress(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set as default address")
    public ResponseEntity<ApiResponse<AddressDto>> setDefault(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        AddressDto address = addressService.setDefaultAddress(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(address, "Default address updated"));
    }
}
