package com.craftistan.user.service;

import com.craftistan.auth.dto.AuthResponse;
import com.craftistan.user.dto.ChangePasswordRequest;
import com.craftistan.user.dto.UpdateProfileRequest;
import com.craftistan.user.entity.User;
import com.craftistan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse.UserDto getProfile(User user) {
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .build();
    }

    @Transactional
    public AuthResponse.UserDto updateProfile(UpdateProfileRequest request, User user) {
        user.setName(request.getName());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        // Email change would need verification, keeping it simple for now

        User saved = userRepository.save(user);

        return AuthResponse.UserDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .avatar(saved.getAvatar())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, User user) {
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public String updateAvatar(String avatarUrl, User user) {
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }
}
