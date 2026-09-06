package com.healthinsurance.customer.dto.response;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MemberResponse {
    private UUID memberId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String relationshipToCustomer;
    private String gender;
}