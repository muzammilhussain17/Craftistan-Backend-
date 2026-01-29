package com.craftistan.user.service;

import com.craftistan.common.exception.ResourceNotFoundException;
import com.craftistan.user.dto.AddressDto;
import com.craftistan.user.entity.Address;
import com.craftistan.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressDto> getUserAddresses(String userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public AddressDto createAddress(AddressDto request, String userId) {
        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .isDefault(false)
                .build();

        // If this is the first address, make it default
        if (addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).isEmpty()) {
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        return toDto(saved);
    }

    @Transactional
    public AddressDto updateAddress(Long id, AddressDto request, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        address.setLabel(request.getLabel());
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());

        Address saved = addressRepository.save(address);
        return toDto(saved);
    }

    @Transactional
    public void deleteAddress(Long id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        addressRepository.delete(address);

        // If deleted address was default, set another as default
        if (address.getIsDefault()) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsDefault(true);
                addressRepository.save(remaining.get(0));
            }
        }
    }

    @Transactional
    public AddressDto setDefaultAddress(Long id, String userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        // Clear existing default
        addressRepository.clearDefaultAddress(userId);

        // Set new default
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);

        return toDto(saved);
    }

    private AddressDto toDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .address(address.getAddress())
                .city(address.getCity())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .build();
    }
}
