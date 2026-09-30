package com.healthinsurance.premium.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_record", indexes = {
    @Index(name = "idx_premium_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_premium_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "operation_type", nullable = false, updatable = false, length = 64)
    private String operationType;

    @Column(name = "entity_type", nullable = false, updatable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, updatable = false, length = 64)
    private String entityId;

    @Column(name = "actor_id", nullable = false, updatable = false, length = 100)
    private String actorId;

    @Column(name = "actor_role", updatable = false, length = 64)
    private String actorRole;

    @Column(name = "source_service", nullable = false, updatable = false, length = 64)
    private String sourceService;

    @Column(name = "endpoint", updatable = false, length = 255)
    private String endpoint;

    @Column(name = "correlation_id", updatable = false, length = 100)
    private String correlationId;

    @Lob
    @Column(name = "before_state", updatable = false, columnDefinition = "TEXT")
    private String beforeState;

    @Lob
    @Column(name = "after_state", updatable = false, columnDefinition = "TEXT")
    private String afterState;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
