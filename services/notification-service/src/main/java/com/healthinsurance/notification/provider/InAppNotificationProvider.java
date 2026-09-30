package com.healthinsurance.notification.provider;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProvider implements NotificationChannelProvider {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationProvider.class);

    @Override
    public boolean supports(NotificationChannel channel) {
        return NotificationChannel.IN_APP.equals(channel);
    }

    @Override
    public void send(Notification notification) {
        log.info("[IN-APP ALERT] User: {} | Title: '{}' | Content: '{}'",
                notification.getRecipient(), notification.getSubject(), notification.getContent());
    }
}
