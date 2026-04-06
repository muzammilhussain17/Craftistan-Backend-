package com.craftistan.user.repository;

import com.craftistan.user.entity.AccountStatus;
import com.craftistan.user.entity.Role;
import com.craftistan.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Auth
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin - User Management
    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByAccountStatus(AccountStatus status, Pageable pageable);

    Page<User> findByRoleAndAccountStatus(Role role, AccountStatus status, Pageable pageable);

    // Admin - Artisan Verification
    Page<User> findByRoleAndIsVerifiedFalse(Role role, Pageable pageable);

    // Admin - Stats
    long countByRole(Role role);

    long countByRoleAndIsVerifiedFalse(Role role);

    long countByAccountStatus(AccountStatus status);
}
