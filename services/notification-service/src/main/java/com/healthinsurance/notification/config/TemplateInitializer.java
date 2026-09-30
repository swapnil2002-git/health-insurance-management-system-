package com.healthinsurance.notification.config;

import com.healthinsurance.notification.domain.NotificationChannel;
import com.healthinsurance.notification.domain.TemplateStatus;
import com.healthinsurance.notification.entity.NotificationTemplate;
import com.healthinsurance.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TemplateInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TemplateInitializer.class);

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        log.info("Checking and seeding default notification templates...");

        seedTemplateIfMissing(
                "POLICY_ISSUED",
                NotificationChannel.EMAIL,
                "Your Health Insurance Policy #{policyNumber} is Issued",
                "Dear Customer, your policy #{policyNumber} has been successfully issued. Effective date: {effectiveDate}, Expiry date: {expiryDate}."
        );

        seedTemplateIfMissing(
                "POLICY_ISSUED",
                NotificationChannel.IN_APP,
                "Policy Issued",
                "Policy #{policyNumber} is active and available in your policy dashboard."
        );

        seedTemplateIfMissing(
                "PREMIUM_PAID",
                NotificationChannel.EMAIL,
                "Receipt for Payment of Premium: ${amount}",
                "Thank you for your payment of ${amount} for policy reference {policyId}. Reference: {gatewayReference}."
        );

        seedTemplateIfMissing(
                "PREMIUM_PAID",
                NotificationChannel.SMS,
                "Payment Received",
                "HIMS: We received your premium payment of ${amount}. Ref: {gatewayReference}."
        );

        seedTemplateIfMissing(
                "CLAIM_SUBMITTED",
                NotificationChannel.EMAIL,
                "Claim #{claimNumber} Submitted Successfully",
                "Your claim #{claimNumber} for amount ${totalClaimAmount} has been received and is under review."
        );

        seedTemplateIfMissing(
                "CLAIM_APPROVED",
                NotificationChannel.EMAIL,
                "Claim #{claimNumber} Approved!",
                "Great news! Your claim #{claimNumber} has been approved for amount ${approvedAmount}."
        );

        seedTemplateIfMissing(
                "CLAIM_REJECTED",
                NotificationChannel.EMAIL,
                "Claim #{claimNumber} Decision",
                "Your claim #{claimNumber} could not be approved. Reason: {reason}."
        );

        seedTemplateIfMissing(
                "CLAIM_SETTLED",
                NotificationChannel.SMS,
                "Claim Settled",
                "HIMS: Claim #{claimNumber} settled for ${approvedAmount}."
        );
    }

    private void seedTemplateIfMissing(String eventType, NotificationChannel channel, String subject, String body) {
        if (templateRepository.findByEventTypeAndChannel(eventType, channel).isEmpty()) {
            NotificationTemplate template = new NotificationTemplate();
            template.setEventType(eventType);
            template.setChannel(channel);
            template.setSubjectTemplate(subject);
            template.setBodyTemplate(body);
            template.setStatus(TemplateStatus.ACTIVE);
            templateRepository.save(template);
            log.info("Seeded notification template for event: {} channel: {}", eventType, channel);
        }
    }
}
