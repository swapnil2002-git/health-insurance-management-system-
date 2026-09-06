package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID customerId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String identificationNumber;
    private String status;
}