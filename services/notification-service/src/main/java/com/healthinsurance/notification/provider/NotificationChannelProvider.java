package com.healthinsurance.notification.provider;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.entity.Notification;

public interface NotificationChannelProvider {

    boolean supports(NotificationChannel channel);

    void send(Notification notification);
}
