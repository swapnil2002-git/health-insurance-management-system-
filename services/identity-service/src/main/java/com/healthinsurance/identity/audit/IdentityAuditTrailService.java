package com.healthinsurance.identity.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityAuditTrailService {

    private final AuditRecordRepository auditRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void recordAudit(String operationType, String entityType, String entityId,
                            Object beforeStateObj, Object afterStateObj,
                            String endpoint, String correlationId) {
        try {
            String actorId = "SYSTEM";
            String actorRole = "SYSTEM_PROCESS";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                actorId = auth.getName();
                if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
                    actorRole = auth.getAuthorities().iterator().next().getAuthority();
                }
            }

            String beforeJson = beforeStateObj != null ? (beforeStateObj instanceof String ? (String) beforeStateObj : objectMapper.writeValueAsString(beforeStateObj)) : null;
            String afterJson = afterStateObj != null ? (afterStateObj instanceof String ? (String) afterStateObj : objectMapper.writeValueAsString(afterStateObj)) : null;

            AuditRecord record = AuditRecord.builder()
                    .operationType(operationType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .sourceService("identity-service")
                    .endpoint(endpoint)
                    .correlationId(correlationId)
                    .beforeState(beforeJson)
                    .afterState(afterJson)
                    .timestamp(Instant.now())
                    .build();

            auditRecordRepository.save(record);
            log.info("AUDIT: Recorded [{}] on {} ID {} by {} ({})", operationType, entityType, entityId, actorId, actorRole);
        } catch (Exception e) {
            log.error("Failed to record audit entry for {} on {}: {}", operationType, entityId, e.getMessage(), e);
        }
    }

    public List<AuditRecord> getAuditTrail(String entityType, String entityId) {
        return auditRecordRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
}
