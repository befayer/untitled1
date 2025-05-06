package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smpp.Session;
import org.smpp.pdu.SubmitSM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {

    private final Session smppSession;

    @Value("${smpp.source_addr}")
    private String sourceAddr;

    @Override
    @Async
    public void send(String message, User user) {
        if (user.getPhoneNumber() == null) {
            log.info("User dont have phoneNumber");
            return;
        }

        try {
            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr(sourceAddr);
            submitSM.setDestAddr(user.getPhoneNumber());
            submitSM.setShortMessage(message);

            smppSession.submit(submitSM);
            log.info("Sms sent successfully: user: {}, message: {}", user.getPhoneNumber(), message);
        } catch (Exception exception){
            log.error("Cant send sms message", exception);
        }
    }
}