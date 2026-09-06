package com.healthinsurance.policy.dto.response;
import lombok.Data;
import java.util.UUID;
@Data
public class PolicyMemberResponse { private UUID policyMemberId; private UUID memberId; }