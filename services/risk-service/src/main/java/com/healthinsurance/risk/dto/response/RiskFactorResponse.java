package com.healthinsurance.risk.dto.response;
import lombok.Data;
import java.util.UUID;

@Data
public class RiskFactorResponse {
    private UUID riskFactorId;
    private String factorName;
    private String factorValue;
    private String description;
}