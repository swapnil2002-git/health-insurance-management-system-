package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "deductible")
public class Deductible {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deductibleId;

    @Column(nullable = false)
    private String name;

    private String description;
}