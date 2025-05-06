package com.fladx.otpservice.service;

import com.fladx.otpservice.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserService userService;
    private final OtpService otpService;

    @Transactional
    public void deleteUserAndOtpCodes(Long userId) {
        otpService.deleteOtpCodeByUserId(userId);
        userService.deleteUser(userId);
    }
}