package com.healthinsurance.notification.repository;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByEventTypeAndChannel(String eventType, NotificationChannel channel);

    Optional<NotificationTemplate> findByEventTypeAndChannelAndStatus(
            String eventType, NotificationChannel channel, TemplateStatus status);

    List<NotificationTemplate> findByEventType(String eventType);

    List<NotificationTemplate> findByStatus(TemplateStatus status);
}
