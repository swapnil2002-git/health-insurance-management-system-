package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "coverage")
public class Coverage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID coverageId;

    @Column(nullable = false)
    private String name;

    private String description;
}