package com.healthinsurance.notification.mapper;

import com.healthinsurance.notification.dto.NotificationRequest;
import com.healthinsurance.notification.dto.NotificationResponse;
import com.healthinsurance.notification.dto.NotificationTemplateRequest;
import com.healthinsurance.notification.dto.NotificationTemplateResponse;
import com.healthinsurance.notification.entity.Notification;
import com.healthinsurance.notification.entity.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Notification toEntity(NotificationRequest request);

    NotificationResponse toResponse(Notification entity);

    List<NotificationResponse> toResponseList(List<Notification> entities);

    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NotificationTemplate toEntity(NotificationTemplateRequest request);

    NotificationTemplateResponse toResponse(NotificationTemplate entity);

    List<NotificationTemplateResponse> toTemplateResponseList(List<NotificationTemplate> entities);
}
