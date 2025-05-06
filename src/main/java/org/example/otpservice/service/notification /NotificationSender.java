import User;

public interface NotificationSender {
    void send(String message, User user);
}