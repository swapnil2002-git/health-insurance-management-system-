package com.healthinsurance.quotation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "quote_member")
public class QuoteMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID quoteMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Quotation quote;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    private String relationship;
    private String gender;
}