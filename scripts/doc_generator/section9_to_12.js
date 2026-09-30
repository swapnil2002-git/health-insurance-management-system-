// Section 9 to 12 module
module.exports = {
  getSection9: function() {
    return `
# 9. SECURITY ARCHITECTURE

## 9.1 Enterprise Security Architecture Model
HIMS implements a defense-in-depth, zero-trust security perimeter combining edge gateway token authentication with decentralized microservice role enforcement.

\`\`\`
[ Angular 17 UI ]
       │  HTTP Request + Authorization: Bearer <JWT>
       ▼
[ Spring Cloud Gateway (8080) ]
       │  1. Verifies JWT HMAC-SHA256 signature
       │  2. Checks expiration
       │  3. Injects claims into downstream headers:
       │     - X-User-Id: 42
       │     - X-Username: underwriter_john
       │     - X-User-Roles: ROLE_UNDERWRITER
       ▼
[ Downstream Microservice (e.g. policy-service) ]
       │  1. JwtAuthenticationFilter parses Bearer token
       │  2. Populates Spring SecurityContextHolder
       │  3. Method-level enforcement: @PreAuthorize("hasRole('ROLE_UNDERWRITER')")
\`\`\`

## 9.2 JSON Web Token (JWT) Specification
Authentication tokens are issued by \`identity-service\` upon verified login and signed using HMAC-SHA256 (\`HS256\`) with an enterprise 256-bit secret key.

### JWT Structure
- **Header**:
\`\`\`json
{
  "alg": "HS256",
  "typ": "JWT"
}
\`\`\`
- **Payload Claims**:
\`\`\`json
{
  "sub": "underwriter_john",
  "userId": 42,
  "email": "john.underwriter@hims-enterprise.com",
  "roles": ["ROLE_UNDERWRITER"],
  "iat": 1727690000,
  "exp": 1727776400
}
\`\`\`
- **Signature**: \`HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secretKey)\`

## 9.3 Role-Based Access Control (RBAC) Matrix

| User Role | Implementation Status | Permitted Operations / Endpoints |
| :--- | :--- | :--- |
| **\`ROLE_ADMIN\`** | ✅ IMPLEMENTED | Full CRUD access to all services, user role provisioning, system settings, global provider creation, and executive reporting. |
| **\`ROLE_CUSTOMER\`** | ✅ IMPLEMENTED | Self-registration, view available plans, generate quotes, view owned policies, pay premiums, upload claim documents, submit claims, download own EOBs. |
| **\`ROLE_AGENT\`** | ✅ IMPLEMENTED | Create and manage quotes for clients, register customer profiles, initiate policy binding requests. |
| **\`ROLE_UNDERWRITER\`** | ✅ IMPLEMENTED | Review medical risk assessments, evaluate policy applications, set premium loading percentages, approve or decline underwriting cases (\`PUT /api/underwriting/{id}/approve\`). |
| **\`ROLE_CLAIMS_OFFICER\`** | ✅ IMPLEMENTED | Review submitted claims, inspect automated validation rule results, execute adjudication calculations, approve or reject claims, authorize settlement disbursements. |
| **\`ROLE_FINANCE_OFFICER\`** | ⚠️ PARTIALLY IMPLEMENTED | View payment transactions, inspect billing schedules, download financial reconciliation reports (largely merged into \`ROLE_ADMIN\` in current UI). |

## 9.4 Method-Level Authorization Examples
Spring Security's \`@EnableMethodSecurity(prePostEnabled = true)\` protects controller methods:
\`\`\`java
// Underwriting Service: Only licensed underwriters can approve cases
@PutMapping("/{id}/approve")
@PreAuthorize("hasAnyRole('ROLE_UNDERWRITER', 'ROLE_ADMIN')")
public ResponseEntity<ApiResponse<UnderwritingCaseDto>> approveCase(@PathVariable Long id) {
    return ResponseEntity.ok(underwritingService.approveCase(id));
}

// Claims Service: Only claims officers can execute manual override adjudications
@PostMapping("/{id}/adjudicate")
@PreAuthorize("hasAnyRole('ROLE_CLAIMS_OFFICER', 'ROLE_ADMIN')")
public ResponseEntity<ApiResponse<AdjudicationResultDto>> adjudicateClaim(@PathVariable Long id) {
    return ResponseEntity.ok(claimAdjudicationService.adjudicateClaim(id));
}
\`\`\`

## 9.5 Password Security & Data Protection
- **BCrypt Hashing**: All user passwords stored in \`identity_db.users\` are hashed using \`BCryptPasswordEncoder\` with an internal cost factor of 10. Passwords are never logged or returned in DTOs.
- **Stateless Session Management**: Microservices operate with \`SessionCreationPolicy.STATELESS\`, eliminating HTTP session hijacking vulnerabilities.

---
`;
  },

  getSection9_to_12_Validation: function() {
    return `
# 10. VALIDATION ARCHITECTURE

HIMS enforces a **Three-Level Validation Architecture** backed by strict **Domain Business Rules** to prevent data corruption and ensure zero garbage data reaches persistence.

\`\`\`
+-----------------------------------------------------------------------------------+
|                        THREE-LEVEL VALIDATION STRATEGY                            |
+-----------------------------------------------------------------------------------+
| Level 1: Angular Client Validation                                                |
| - Reactive Forms (Validators.required, Validators.pattern, Validators.maxLength)  |
| - HTML5 element constraints (maxlength="10", min="1", max="10000000")             |
| - Instant visual feedback (red borders, disabled submit buttons)                  |
+-----------------------------------------------------------------------------------+
                                      │ HTTP Request
                                      ▼
+-----------------------------------------------------------------------------------+
| Level 2: Backend API / DTO Validation (JSR-380 / Bean Validation)                 |
| - Annotations: @NotBlank, @Size(max=50), @Pattern, @Positive, @Email             |
| - Automatic intercept by MethodArgumentNotValidException                          |
| - Standardized 400 Bad Request JSON response with field-by-field error dictionary |
+-----------------------------------------------------------------------------------+
                                      │ Validated DTO
                                      ▼
+-----------------------------------------------------------------------------------+
| Business Logic & Domain Rule Validation                                           |
| - Policy active status check via OpenFeign                                        |
| - Provider license & network accreditation verification                           |
| - Admission date <= Discharge date, Deductible <= Claimed amount                  |
+-----------------------------------------------------------------------------------+
                                      │ Entity Persistence
                                      ▼
+-----------------------------------------------------------------------------------+
| Level 3: Database Schema Constraints                                              |
| - NOT NULL, UNIQUE constraints (e.g. claim_number, policy_number, email)          |
| - Column max length enforcement (VARCHAR(50), VARCHAR(10))                        |
| - Foreign key referential integrity within service boundaries                     |
+-----------------------------------------------------------------------------------+
\`\`\`

## 10.1 Level 1: Frontend Angular Validation
In the Angular client (\`frontend/hims-ui\`), all user inputs are governed by Angular Reactive Forms with HTML5 bounds:
\`\`\`typescript
this.claimForm = this.fb.group({
  policyNumber: ['', [Validators.required, Validators.pattern('^POL-[0-9]{4}-[0-9]{4,}$'), Validators.maxLength(50)]],
  providerCode: ['', [Validators.required, Validators.pattern('^[A-Z0-9-]{3,50}$'), Validators.maxLength(50)]],
  claimedAmount: [null, [Validators.required, Validators.min(1), Validators.max(5000000)]],
  admissionDate: ['', [Validators.required]],
  dischargeDate: ['', [Validators.required]]
});
\`\`\`
- Form submit buttons are bound to \`[disabled]="!claimForm.valid"\`, preventing accidental submission of empty or malformed inputs.
- All input fields enforce explicit \`maxlength\` HTML attributes to prevent buffer flooding when users paste large payloads.

## 10.2 Level 2: Backend DTO JSR-380 Annotations
All incoming REST DTOs are validated at the controller boundary using \`@Valid\`:

\`\`\`java
public class CustomerRegistrationDto {

    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\\\s]+$", message = "First name can only contain alphabetic characters")
    private String firstName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be a valid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "PAN number is mandatory")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must follow standard format (e.g. ABCDE1234F)")
    private String panNumber;

    @NotNull(message = "Date of birth is mandatory")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
}
\`\`\`

## 10.3 Level 3: Database Schema Validation
At the persistence tier, MySQL constraints guarantee structural integrity:
- \`NOT NULL\` on critical identifiers (\`policy_number\`, \`claim_number\`, \`claimed_amount\`).
- \`UNIQUE\` indexes on business keys preventing duplicate accounts or transactions.
- Length specifications (\`VARCHAR(10)\` for PAN, \`VARCHAR(12)\` for Aadhaar, \`VARCHAR(50)\` for codes).

## 10.4 Business Domain Validations
Beyond syntactic checks, services execute domain-specific semantic validations:
1. **Admission Date Temporal Consistency**: \`dischargeDate >= admissionDate\`.
2. **Policy Coverage Window**: \`policy.startDate <= admissionDate <= policy.endDate\`.
3. **Policy Active Status**: Only policies with \`status == 'ACTIVE'\` are eligible for claim adjudication.
4. **Duplicate Claim Detection**: No two claims with identical patient, provider, and admission date can be submitted.

---
`;
  },

  getSection11: function() {
    return `
# 11. SAGA PATTERN & TRANSACTION MANAGEMENT

## 11.1 The Distributed Transaction Problem
In a microservices architecture, a single business workflow—such as policy issuance and billing schedule creation—spans multiple independent databases (\`policy_db\`, \`premium_db\`, \`payment_db\`). Traditional ACID transactions with 2-Phase Commit (2PC) are unsuitable because:
- They cause heavy network latency and distributed deadlocks.
- They severely degrade availability (violating CAP theorem principles).
- MySQL 2PC across microservices introduces tight coupling.

To solve this, HIMS implements **Choreography-based Sagas** combined with the **Transactional Outbox Pattern**.

\`\`\`
+---------------------------------------------------------------------------------------+
|                            HIMS CHOREOGRAPHY SAGA FLOW                                |
+---------------------------------------------------------------------------------------+
  [ Policy Service ]
        │  1. Issues Policy in Local DB (policy_db: policies, status='ISSUED')
        │  2. Inserts 'PolicyIssuedEvent' into policy_db.outbox_events
        ▼
  [ Outbox Relayer (PolicyOutboxPublisher) ]
        │  Polls pending events every 5 seconds -> Publishes to Kafka "policy-events"
        ▼
   ═══════════════════════════════════════════════════════════════════════════════════
                           Kafka Topic: policy-events
   ═══════════════════════════════════════════════════════════════════════════════════
        │
        ├─────────────────────────────────────────────────┐
        ▼ (Success Path)                                  ▼ (Failure / Exception Path)
  [ Premium Service ]                               [ Premium Service ]
  - Consumes PolicyIssuedEvent                      - Catastrophic error creating schedule
  - Generates Installment Schedule                  - Publishes "PremiumScheduleFailedEvent"
  - Persists in premium_db                          - Outbox Relayer -> Kafka "policy-events"
        │                                                 │
        ▼                                                 ▼ (COMPENSATING TRANSACTION)
  [ Payment Service ]                               [ Policy Service ]
  - Customer completes payment                      - PolicySagaCompensationConsumer listens
  - Emits "PremiumPaidEvent"                        - Executes compensatePolicyIssuance()
        │                                           - Rolls back Policy status -> CANCELLED
        ▼                                           - Re-opens quote for customer edit
  [ Policy Service ]
  - Activates Policy (status='ACTIVE')
\`\`\`

## 11.2 Compensating Transaction Code Walkthrough
In \`policy-service\`, compensation is handled by \`PolicySagaCompensationConsumer\`:
\`\`\`java
@Component
@Slf4j
public class PolicySagaCompensationConsumer {

    @Autowired
    private PolicyRepository policyRepository;

    @KafkaListener(topics = "policy-events", groupId = "policy-saga-group")
    public void handlePolicyEvents(String message) {
        EventEnvelope event = parseEnvelope(message);
        if ("PremiumScheduleFailedEvent".equals(event.getEventType())) {
            String policyNumber = event.getAggregateId();
            log.warn("SAGA COMPENSATION TRIGGERED for Policy: {}", policyNumber);
            Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

            // Compensating action: cancel policy and log failure reason
            policy.setStatus(PolicyStatus.CANCELLED);
            policy.setUpdatedReason("SAGA Compensation: Premium schedule creation failed");
            policyRepository.save(policy);
            log.info("Policy {} successfully rolled back to CANCELLED state.", policyNumber);
        }
    }
}
\`\`\`

## 11.3 Transactional Outbox Implementation
Implemented in \`claims-service\`, \`payment-service\`, and \`policy-service\`:
1. **Local Transaction**: The business entity and the \`outbox_events\` row are committed together:
\`\`\`java
@Transactional
public Policy issuePolicy(IssuePolicyRequest request) {
    Policy policy = createPolicyEntity(request);
    policyRepository.save(policy);

    OutboxEvent outbox = OutboxEvent.builder()
        .aggregateType("POLICY")
        .aggregateId(policy.getPolicyNumber())
        .eventType("PolicyIssuedEvent")
        .payload(objectMapper.writeValueAsString(policy))
        .status("PENDING")
        .retryCount(0)
        .build();
    outboxRepository.save(outbox);
    return policy;
}
\`\`\`
2. **Scheduled Relayer**: A scheduled background thread polls for \`PENDING\` records:
\`\`\`java
@Scheduled(fixedDelay = 5000)
public void publishPendingOutboxEvents() {
    List<OutboxEvent> events = outboxRepository.findByStatus("PENDING");
    for (OutboxEvent event : events) {
        try {
            kafkaTemplate.send("policy-events", event.getAggregateId(), event.getPayload());
            event.setStatus("PUBLISHED");
            outboxRepository.save(event);
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            if (event.getRetryCount() >= 5) event.setStatus("FAILED");
            outboxRepository.save(event);
        }
    }
}
\`\`\`

---
`;
  },

  getSection12: function() {
    return `
# 12. RESILIENCE AND FAULT TOLERANCE

## 12.1 Circuit Breaker & Retry Patterns (Resilience4j)
Distributed microservices are susceptible to cascading network latencies and transient downstream service crashes. HIMS integrates **Resilience4j** in the \`claims-service\` to protect synchronous OpenFeign invocations targeting \`policy-service\` and \`provider-service\`.

\`\`\`
[ Claim Adjudication Engine ]
             │
             ▼ Calls PolicyServiceClient.getPolicyByNumber()
    ┌────────────────────────────────────────────────────────┐
    │          Resilience4j Circuit Breaker Proxy            │
    │                                                        │
    │  State: CLOSED (Normal Operation)                      │
    │  - Failure Rate Threshold: 50%                         │
    │  - Sliding Window: 10 calls                            │
    │  - If failures exceed 50% -> Transition to OPEN        │
    │  - Wait Duration in OPEN: 5000ms                       │
    │  - Transition to HALF-OPEN to test downstream recovery │
    └────────────────────────────────────────────────────────┘
             │
             ├── If OPEN or Timeout ──► Fallback Method Invoked
             ▼ (If CLOSED)
    [ Policy Service (8088) ]
\`\`\`

## 12.2 Configuration in \`config-repo/claims-service.yml\`
\`\`\`yaml
resilience4j:
  circuitbreaker:
    instances:
      policyServiceBreaker:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 5000ms
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
      providerServiceBreaker:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5000ms
  retry:
    instances:
      policyServiceRetry:
        maxAttempts: 3
        waitDuration: 1000ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2.0
\`\`\`

## 12.3 Feign Client with CircuitBreaker & Fallback Implementation
\`\`\`java
@FeignClient(name = "policy-service", fallback = PolicyServiceFallback.class)
public interface PolicyServiceClient {

    @GetMapping("/api/policies/number/{policyNumber}")
    @CircuitBreaker(name = "policyServiceBreaker", fallbackMethod = "getPolicyFallback")
    @Retry(name = "policyServiceRetry")
    ApiResponse<PolicyDto> getPolicyByNumber(@PathVariable("policyNumber") String policyNumber);

    default ApiResponse<PolicyDto> getPolicyFallback(String policyNumber, Throwable t) {
        log.error("Fallback invoked for PolicyServiceClient: {}. Reason: {}", policyNumber, t.getMessage());
        return ApiResponse.<PolicyDto>builder()
            .success(false)
            .message("Policy Service is temporarily unavailable. Claim queued for offline verification.")
            .data(null)
            .build();
    }
}
\`\`\`

---
`;
  }
};
