package com.healthinsurance.quotation.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class QuoteMemberRequest {
    @NotBlank(message = "Member name is required")
    private String memberName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private String relationship;
    private String gender;
}