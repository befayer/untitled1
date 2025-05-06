package com.fladx.otpservice.repository;

import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(UserRole role);
}