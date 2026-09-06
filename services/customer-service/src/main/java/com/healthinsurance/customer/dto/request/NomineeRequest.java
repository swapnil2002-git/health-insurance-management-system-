package com.healthinsurance.customer.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import java.time.LocalDate;

@Data
public class NomineeRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Relationship is required")
    private String relationship;
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
}