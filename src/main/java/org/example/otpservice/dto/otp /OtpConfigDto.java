/**
 * DTO for {@link com.fladx.otpservice.model.otp.OtpConfig}
 */
public record OtpConfigDto(Integer codeLength, Long ttlSeconds) {
}