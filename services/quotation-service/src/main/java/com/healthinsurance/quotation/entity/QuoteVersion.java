package com.healthinsurance.quotation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "quote_version")
public class QuoteVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID versionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quotation quote;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private BigDecimal premiumAtVersion;

    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}