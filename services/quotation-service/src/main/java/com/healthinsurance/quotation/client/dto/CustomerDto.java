package com.healthinsurance.quotation.client.dto;
import lombok.Data;
import java.util.UUID;
@Data
public class CustomerDto {
    private UUID customerId;
    private String status;
}