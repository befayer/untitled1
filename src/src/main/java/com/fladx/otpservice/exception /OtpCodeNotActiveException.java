package com.fladx.otpservice.exception;

public class OtpCodeNotActiveException extends RuntimeException{
    public OtpCodeNotActiveException() {
        super("OTP code not active.");
    }
}