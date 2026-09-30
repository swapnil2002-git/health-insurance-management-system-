package com.healthinsurance.notification.provider;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsNotificationProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationProvider.class);

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.SMS.equals(channel);
    }

    @Override
    public void send(Notification notification) {
        log.info("[SMS DISPATCH] To: {} | Content: '{}'",
                notification.getRecipient(), notification.getContent());
    }
}
