package com.healthinsurance.product.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class RuleResponse {
    private UUID id;
    private String name;
    private String description;
}