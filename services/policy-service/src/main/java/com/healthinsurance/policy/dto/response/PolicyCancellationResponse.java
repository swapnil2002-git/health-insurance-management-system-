package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;
@Data
public class PolicyCancellationResponse { private UUID cancellationId; private String reason; private Instant cancelledAt; }