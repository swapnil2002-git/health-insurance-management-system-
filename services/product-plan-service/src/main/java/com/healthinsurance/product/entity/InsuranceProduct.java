package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
import java.util.Set;

@Data
@Entity
@Table(name = "insurance_product")
public class InsuranceProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
    
    @Column(nullable = false)
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InsurancePlan> plans;
}