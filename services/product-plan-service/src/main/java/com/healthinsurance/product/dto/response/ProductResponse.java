package com.healthinsurance.product.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class ProductResponse {
    private UUID productId;
    private String name;
    private String description;
    private String status;
}