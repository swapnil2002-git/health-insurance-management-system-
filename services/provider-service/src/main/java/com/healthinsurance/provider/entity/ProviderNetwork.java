package com.healthinsurance.provider.entity;

import com.healthinsurance.provider.enums.NetworkStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "provider_network", uniqueConstraints = {
    @UniqueConstraint(name = "uk_network_name", columnNames = {"network_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "network_id", updatable = false, nullable = false)
    private UUID networkId;

    @Column(name = "network_name", nullable = false, unique = true)
    private String networkName;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NetworkStatus status = NetworkStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "network", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProviderNetworkMapping> mappings = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
        if (this.status == null) this.status = NetworkStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}