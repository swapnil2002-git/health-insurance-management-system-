package com.healthinsurance.quotation.entity;

import com.healthinsurance.quotation.enums.QuoteStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "quotation")
public class Quotation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID quoteId;

    // References to external microservices (No JPA @ManyToOne here!)
    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private UUID planId;

    @Column(nullable = false, unique = true)
    private String quoteNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.DRAFT;

    private BigDecimal totalPremium;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant expiresAt;
    private Instant acceptedAt;
    private Instant rejectedAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuoteMember> members;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuotePremium> premiums;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QuoteVersion> versions;
}