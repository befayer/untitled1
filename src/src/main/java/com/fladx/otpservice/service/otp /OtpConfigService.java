package com.fladx.otpservice.service.otp;

import com.fladx.otpservice.dto.otp.OtpConfigDto;
import com.fladx.otpservice.model.otp.OtpConfig;
import com.fladx.otpservice.repository.OtpConfigRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OtpConfigService {


    private final OtpConfigRepository otpConfigRepository;

    @Transactional(readOnly = true)
    public OtpConfigDto getOtpConfigDto() {
        var config = getOtpConfig();

        return new OtpConfigDto(config.getCodeLength(), config.getTtlSeconds());
    }

    @Transactional
    public OtpConfigDto update(OtpConfigDto req) {
        var config = getOtpConfig();

        if (req.codeLength() != null) {
            config.setCodeLength(req.codeLength());
        }
        if (req.ttlSeconds() != null) {
            config.setTtlSeconds(req.ttlSeconds());
        }

        log.info("Otp config updated: {}", config);
        return new OtpConfigDto(config.getCodeLength(), config.getTtlSeconds());
    }

    @Transactional(readOnly = true)
    public OtpConfig getOtpConfig() {
        return otpConfigRepository.findOtpConfigById(1)
                .orElseThrow(() -> {
                    log.info("OTP config not found");
                    return new EntityNotFoundException("Otp config not found");
                });
    }

    @Transactional(readOnly = true)
    public boolean existsConfig() {
        return otpConfigRepository.existsById(1);
    }

    @Transactional
    public OtpConfigDto create(OtpConfigDto req) {
        if (existsConfig()) {
            log.info("Otp config already exists");
            throw new EntityExistsException("Otp config already exists");
        }

        OtpConfig otpConfig = new OtpConfig();
        otpConfig.setCodeLength(req.codeLength());
        otpConfig.setTtlSeconds(req.ttlSeconds());

        otpConfig = otpConfigRepository.save(otpConfig);
        log.info("Otp config created");
        return new OtpConfigDto(otpConfig.getCodeLength(), otpConfig.getTtlSeconds());
    }
}