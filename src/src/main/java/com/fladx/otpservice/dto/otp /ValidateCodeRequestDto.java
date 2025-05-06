package com.fladx.otpservice.dto.otp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidateCodeRequestDto(
        @NotNull @NotBlank String code,
        @NotNull Long operationId
) {
}