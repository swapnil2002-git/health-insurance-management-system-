package com.healthinsurance.policy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerMemberDto {
    private UUID memberId;
    private String firstName;
    private String lastName;
    private String relationshipToCustomer;
    private String gender;
}
