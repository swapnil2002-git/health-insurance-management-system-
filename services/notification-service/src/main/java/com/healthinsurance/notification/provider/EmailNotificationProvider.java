package com.healthinsurance.notification.provider;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationProvider.class);

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.EMAIL.equals(channel);
    }

    @Override
    public void send(Notification notification) {
        log.info("[EMAIL DISPATCH] To: {} | Subject: '{}' | Content: '{}'",
                notification.getRecipient(), notification.getSubject(), notification.getContent());
    }
}
