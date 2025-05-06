package com.fladx.otpservice.controller;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.service.AuthService;
import com.fladx.otpservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody UserDto request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserDto request) {
        return authService.login(request);
    }
}