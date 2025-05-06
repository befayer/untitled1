package com.fladx.otpservice.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtpCodeExpiredException extends RuntimeException {
    public OtpCodeExpiredException() {
        super("OTP code has expired");
    }
}