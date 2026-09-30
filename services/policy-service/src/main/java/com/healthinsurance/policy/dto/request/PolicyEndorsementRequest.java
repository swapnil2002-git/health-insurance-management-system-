package com.healthinsurance.policy.dto.request;

import com.healthinsurance.policy.enums.EndorsementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyEndorsementRequest {
    @NotNull(message = "Endorsement type is required")
    private EndorsementType endorsementType;

    @NotBlank(message = "Endorsement description is required")
    @Size(min = 3, max = 500, message = "Endorsement description must be between 3 and 500 characters")
    private String description;

    private Map<String, Object> changeData;

    @Size(max = 100, message = "Requested by cannot exceed 100 characters")
    private String requestedBy;
}