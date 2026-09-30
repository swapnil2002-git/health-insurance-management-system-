package com.healthinsurance.notification.repository;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.NotificationStatus;
import com.healthinsurance.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByReferenceId(UUID referenceId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByRecipient(String recipient);

    boolean existsByEventIdAndChannel(String eventId, NotificationChannel channel);

    Optional<Notification> findByEventIdAndChannel(String eventId, NotificationChannel channel);
}
