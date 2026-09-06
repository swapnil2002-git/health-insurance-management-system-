package com.healthinsurance.product.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PlanRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotBlank(message = "Plan name is required")
    private String name;
    
    private String description;
}