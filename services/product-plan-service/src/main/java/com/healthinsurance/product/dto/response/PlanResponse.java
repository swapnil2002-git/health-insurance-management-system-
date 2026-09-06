package com.healthinsurance.product.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class PlanResponse {
    private UUID planId;
    private UUID productId;
    private String name;
    private String description;
}