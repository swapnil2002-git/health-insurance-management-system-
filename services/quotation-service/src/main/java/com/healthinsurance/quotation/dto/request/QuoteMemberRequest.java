package com.healthinsurance.quotation.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class QuoteMemberRequest {
    @NotBlank(message = "Member name is required")
    @Size(min = 2, max = 100, message = "Member name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Member name must contain only letters, spaces, hyphens, or apostrophes")
    private String memberName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Relationship is required")
    @Pattern(regexp = "^(SELF|SPOUSE|CHILD|PARENT|SIBLING|DEPENDENT|Self|Spouse|Child|Parent|Sibling|Dependent)$", message = "Relationship must be a valid relation (e.g. SELF, SPOUSE, CHILD, PARENT, DEPENDENT)")
    private String relationship;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^(MALE|FEMALE|OTHER|Male|Female|Other)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
}