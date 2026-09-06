package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "exclusion")
public class Exclusion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID exclusionId;

    @Column(nullable = false)
    private String name;

    private String description;
}