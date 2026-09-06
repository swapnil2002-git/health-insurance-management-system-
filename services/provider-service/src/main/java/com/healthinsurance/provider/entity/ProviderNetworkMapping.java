package com.healthinsurance.provider.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_network_mapping", uniqueConstraints = {
    @UniqueConstraint(name = "uk_provider_network_mapping", columnNames = {"provider_id", "network_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetworkMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mapping_id", updatable = false, nullable = false)
    private UUID mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_id", nullable = false)
    private ProviderNetwork network;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "joined_date", nullable = false)
    private Instant joinedDate = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
        if (this.joinedDate == null) this.joinedDate = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}