package com.healthinsurance.quotation.client.dto;
import lombok.Data;
import java.util.UUID;
@Data
public class PlanDto {
    private UUID planId;
    private String name;
}