package com.healthinsurance.policy.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "policy_member")
public class PolicyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID policyMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    // External Reference to Customer Service Member
    @Column(nullable = false)
    private UUID memberId;
}