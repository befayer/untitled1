package org.example.otpservice;

import com.fladx.otpservice.model.otp.OtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpConfigRepository extends JpaRepository<OtpConfig, Integer> {
    Optional<OtpConfig> findOtpConfigById(Integer id);
}