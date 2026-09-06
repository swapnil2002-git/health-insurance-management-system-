package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class BeneficiaryResponse {
    private UUID beneficiaryId;
    private String name;
    private double allocationPercentage;
    private String relationship;
}