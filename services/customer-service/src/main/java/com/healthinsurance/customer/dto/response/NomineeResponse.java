package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class NomineeResponse {
    private UUID nomineeId;
    private String name;
    private String relationship;
    private LocalDate dateOfBirth;
}