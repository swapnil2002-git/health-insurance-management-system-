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
@Table(name = "quote_premium")
public class QuotePremium {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID quotePremiumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quotation quote;

    @Column(nullable = false)
    private BigDecimal calculatedPremium;

    private String calculationDetails;

    @Column(nullable = false)
    private Instant calculatedAt = Instant.now();
}