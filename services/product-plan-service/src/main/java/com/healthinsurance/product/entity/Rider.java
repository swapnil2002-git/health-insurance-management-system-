package com.healthinsurance.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "rider")
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID riderId;

    @Column(nullable = false)
    private String name;

    private String description;
}