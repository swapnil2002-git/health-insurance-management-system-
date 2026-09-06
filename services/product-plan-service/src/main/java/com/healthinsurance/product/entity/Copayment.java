package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "copayment")
public class Copayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID copaymentId;

    @Column(nullable = false)
    private String name;

    private String description;
}