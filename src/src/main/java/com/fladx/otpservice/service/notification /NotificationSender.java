package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;

public interface NotificationSender {
    void send(String message, User user);
}