package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class TelegramNotificationSender implements NotificationSender, SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${telegram.bot.token}")
    private String token;

    private TelegramClient telegramClient;

    @PostConstruct
    public void init() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    @Override
    @Async
    public void send(String message, User user) {
        if (user.getTelegramId() == null) {
            log.info("Telegram id is null. Notify dont send");
            return;
        }

        var sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(message)
                .build();
        try {
            telegramClient.execute(sendMessage);
            log.info("Sent telegram message: {}", sendMessage);
        } catch (TelegramApiException e) {
            log.error("Telegram API exception", e);
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        Message message = update.getMessage();
        if (update.hasMessage() && message.hasText()) {
            String messageText = message.getText();
            Long chatId = message.getChatId();
            if (messageText.equals("/start")) {
                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chatId)
                        .text("Вы успешно зарегистрировались в системе для получения уведомлений!\n" +
                                "Ваш id: " + chatId)
                        .build();

                try {
                    telegramClient.execute(sendMessage);
                    log.info("Sent telegram message: {}", sendMessage.getText());
                } catch (TelegramApiException e) {
                    log.error("Telegram API exception", e);
                }
            }
        }
    }
}