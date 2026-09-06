package com.healthinsurance.quotation.dto.response;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class QuoteMemberResponse {
    private UUID quoteMemberId;
    private String memberName;
    private LocalDate dateOfBirth;
    private String relationship;
    private String gender;
}