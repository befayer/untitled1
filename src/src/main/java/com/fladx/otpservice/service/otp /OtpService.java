package com.fladx.otpservice.service.otp;

import com.fladx.otpservice.dto.otp.GenerateCodeRequestDto;
import com.fladx.otpservice.dto.otp.ValidateCodeRequestDto;
import com.fladx.otpservice.exception.OtpCodeExpiredException;
import com.fladx.otpservice.exception.OtpCodeNotActiveException;
import com.fladx.otpservice.model.otp.OtpCode;
import com.fladx.otpservice.model.otp.OtpConfig;
import com.fladx.otpservice.model.otp.OtpStatus;
import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.repository.OtpCodeRepository;
import com.fladx.otpservice.service.UserService;
import com.fladx.otpservice.service.notification.NotificationSender;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpConfigService otpConfigService;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();
    private final UserService userService;
    private final OtpCodeRepository otpCodeRepository;
    private final List<NotificationSender> notificationSenders;

    @Transactional
    public String generateOtpCode(GenerateCodeRequestDto requestDto) {
        OtpConfig cfg = otpConfigService.getOtpConfig();
        String code = generateRandomCode(cfg.getCodeLength());
        User user = userService.getAuthorizedUser();

        OtpCode otpCode = OtpCode.builder()
                .code(code)
                .user(user)
                .status(OtpStatus.ACTIVE)
                .operation_id(requestDto.operationId())
                .expiresAt(LocalDateTime.now().plusSeconds(cfg.getTtlSeconds()))
                .build();

        otpCodeRepository.save(otpCode);
        log.info("Generated OTP code: {}, user: {}", code, user);
        notificationSenders.forEach(notificationSender -> notificationSender.send("Ваш код подтверждения: " + code, user));
        return code;
    }

    @Transactional
    public void validateCode(ValidateCodeRequestDto requestDto) {
        User user = userService.getAuthorizedUser();

        OtpCode otpCode = getOtpCodeWithUser(requestDto.code(), user.getId());
        if (!otpCode.getStatus().equals(OtpStatus.ACTIVE)) {
            log.info("OTP code {} has not been active.", otpCode);
            throw new OtpCodeNotActiveException();
        }

        if (isExpiredOtpCode(otpCode)) {
            log.info("OTP code {} has expired.", otpCode);
            throw new OtpCodeExpiredException();
        }

        otpCode.setStatus(OtpStatus.USED);
        log.info("OTP code {} has been used.", otpCode);
    }

    @Transactional(readOnly = true)
    public OtpCode getOtpCodeWithUser(String code, Long userId) {
        return otpCodeRepository.findByCodeAndUserId(code, userId)
                .orElseThrow(() -> {
                    log.info("Otp code not found: code={}, userId={}", code, userId);
                    return new EntityNotFoundException("OtpCode not found");
                });
    }

    @Transactional(readOnly = true)
    public OtpCode getOtpCode(String code) {
        return otpCodeRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.info("Otp code not found: code={}", code);
                    return new EntityNotFoundException("OtpCode not found");
                });
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }

    private boolean isExpiredOtpCode(OtpCode otpCode) {
        return otpCode.getExpiresAt().isBefore(LocalDateTime.now());
    }

    @Scheduled(fixedDelayString = "${otp.check-expired-delay-ms}")
    @Transactional
    public void checkOtpCodes() {
        int expireActiveCodes = otpCodeRepository.expireActiveCodes(LocalDateTime.now());
        log.info("OTP codes expired: {}", expireActiveCodes);
    }

    @Transactional
    public void deleteOtpCodeByUserId(Long userId) {
        otpCodeRepository.deleteByUser_Id(userId);
    }
}