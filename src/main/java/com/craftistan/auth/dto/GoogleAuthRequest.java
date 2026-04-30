package com.craftistan.auth.dto;

import com.craftistan.user.entity.Role;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    /** The credential (ID token) returned by Google Identity Services on the frontend. */
    private String credential;

    /** Optional: BUYER (default) or ARTISAN — lets users self-select on sign-up. */
    private Role role;
}
