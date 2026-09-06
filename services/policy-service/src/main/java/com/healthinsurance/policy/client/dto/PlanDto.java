package com.healthinsurance.policy.client.dto;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PlanDto {
    private UUID planId;
    private List<PlanCoverageDto> coverages;
}