package com.healthinsurance.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "insured_member")
public class InsuredMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String relationshipToCustomer;
    private String gender;
}