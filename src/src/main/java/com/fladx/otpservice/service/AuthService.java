package com.fladx.otpservice.service;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.exception.AdminAlreadyExistsException;
import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.model.user.UserRole;
import com.fladx.otpservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public String register(UserDto request) {
        if (request.role() == UserRole.ADMIN) {
            synchronized (this) {
                if (userService.adminExists()) {
                    throw new AdminAlreadyExistsException("Admin user already exists");
                }
                return registerUser(request);
            }
        }
        return registerUser(request);
    }

    private String registerUser(UserDto request) {
        User user = userService.create(request);
        log.info("User registered: {}", user);
        return jwtTokenProvider.generateToken(user);
    }

    @Transactional(readOnly = true)
    public String login(UserDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        User user = userService.findByUsername(request.username());
        log.info("User authenticated: {}", user.getUsername());
        return jwtTokenProvider.generateToken(user);
    }
}