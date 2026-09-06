package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;
@Data
public class PolicyEndorsementResponse { private UUID endorsementId; private String description; private Instant appliedAt; }