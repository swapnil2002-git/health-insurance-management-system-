package com.healthinsurance.policy.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class PolicyMemberRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;
}