package com.fladx.otpservice.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smpp.Connection;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Configuration
public class SmppEmulatorConfig {

    @Value("${smpp.host}")
    private String host;

    @Value("${smpp.port}")
    private int port;

    @Value("${smpp.system_id}")
    private String systemId;

    @Value("${smpp.password}")
    private String password;

    @Value("${smpp.system_type}")
    private String systemType;

    @Value("${smpp.source_addr}")
    private String sourceAddr;

    private final AtomicReference<Session> sessionHolder = new AtomicReference<>();

    @Bean
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public Session smppSession() {
        try {
            Connection connection = new TCPIPConnection(host, port);
            Session session = new Session(connection);

            BindRequest bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34);
            bindRequest.setAddressRange(sourceAddr);

            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new RuntimeException("Bind failed with status: " + bindResponse.getCommandStatus());
            }

            sessionHolder.set(session);
            log.info("SMPP session established successfully");
            return session;
        } catch (Exception e) {
            log.error("Failed to establish SMPP session", e);
            throw new RuntimeException("Failed to establish SMPP session", e);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void checkSession() {
        Session session = sessionHolder.get();
        try {
            if (session == null || !session.isBound()) {
                log.warn("SMPP session is not active, attempting to reconnect...");
                smppSession();
            }
        } catch (Exception e) {
            log.error("SMPP session check failed", e);
        }
    }
}