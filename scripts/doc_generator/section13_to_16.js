// Section 13 to 16 module
module.exports = {
  getSection13: function() {
    return `
# 13. IDEMPOTENCY IMPLEMENTATION

## 13.1 The Double-Submit & Duplicate Payment Dilemma
In distributed financial and healthcare systems, network instability and aggressive client retries can lead to catastrophic duplicate operations:
- A user clicks "Pay Premium" twice in rapid succession.
- A network drop occurs after the payment gateway debits the card but before the HTTP 200 response reaches the browser.
- A hospital automated interface resubmits an emergency cashless claim after a 5-second socket timeout.

To prevent duplicate charges or duplicate claim adjudications, HIMS implements **Header-driven Distributed Idempotency** in both \`payment-service\` and \`claims-service\`.

\`\`\`
[ Client / Angular UI ]
       │
       │ HTTP POST /api/payments/process
       │ Header: X-Idempotency-Key: "c6a1b2d3-9f8e-4a7b-b892-123456789abc"
       │ Body: { policyId: 89, amount: 14500.00 }
       ▼
[ Payment Service / Idempotency Filter ]
       │
       ├── 1. Compute SHA-256 Hash of Request Body
       ├── 2. Query idempotency_records WHERE idempotency_key = key
       │
       ├─► [Record NOT FOUND]:
       │   - Insert record (status='PROCESSING', request_hash=hash)
       │   - Execute payment transaction against gateway
       │   - Save payment entity
       │   - Update record (status='COMPLETED', response_body=jsonResponse)
       │   - Return 201 Created to caller
       │
       ├─► [Record FOUND with status='PROCESSING']:
       │   - Reject with HTTP 409 Conflict ("Concurrent request in progress")
       │
       └─► [Record FOUND with status='COMPLETED']:
           - Compare incoming request_hash with stored request_hash
           - If hashes MATCH: Return cached response_body with HTTP 200 OK (Zero duplicate charge!)
           - If hashes DIFFER: Reject with HTTP 400 Bad Request ("Key reused for different payload")
\`\`\`

## 13.2 Database Schema: \`idempotency_records\`
\`\`\`sql
CREATE TABLE idempotency_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    request_hash VARCHAR(64) NOT NULL,
    response_body TEXT NULL,
    status VARCHAR(20) NOT NULL, -- 'PROCESSING', 'COMPLETED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

## 13.3 Implementation Code Walkthrough
\`\`\`java
@Service
@Transactional
public class IdempotentPaymentService {

    @Autowired
    private IdempotencyRecordRepository idempotencyRepository;

    public PaymentResponse processIdempotentPayment(String idempotencyKey, PaymentRequest request) {
        String requestHash = Sha256Utils.hash(objectMapper.writeValueAsString(request));

        Optional<IdempotencyRecord> existing = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if ("PROCESSING".equals(record.getStatus())) {
                throw new ConcurrentRequestException("Payment is already being processed for key: " + idempotencyKey);
            }
            if (!record.getRequestHash().equals(requestHash)) {
                throw new InvalidIdempotencyKeyException("Idempotency key reused with different request payload!");
            }
            // Return cached response directly
            return objectMapper.readValue(record.getResponseBody(), PaymentResponse.class);
        }

        // Lock key with PROCESSING status
        IdempotencyRecord newRecord = IdempotencyRecord.builder()
            .idempotencyKey(idempotencyKey)
            .requestHash(requestHash)
            .status("PROCESSING")
            .build();
        idempotencyRepository.save(newRecord);

        // Execute core payment transaction
        PaymentResponse response = executeGatewayCharge(request);

        // Cache completed result
        newRecord.setStatus("COMPLETED");
        newRecord.setResponseBody(objectMapper.writeValueAsString(response));
        idempotencyRepository.save(newRecord);

        return response;
    }
}
\`\`\`

---
`;
  },

  getSection14: function() {
    return `
# 14. AUDIT AND LOGGING ARCHITECTURE

## 14.1 Enterprise Audit Trail Strategy
Regulatory frameworks (such as HIPAA and Insurance Regulatory Development Authorities) require strict auditing of all operational and financial events. HIMS implements automated auditing at both the **database level** and the **application aspect level**.

### Database Entity Auditing
Persistent entities inherit from an abstract base class or implement audit fields:
- \`created_at\`: Timestamp of initial creation.
- \`updated_at\`: Automatically updated by database trigger or Hibernate \`@UpdateTimestamp\`.
- \`created_by\` / \`updated_by\`: User ID or username resolved from the Spring Security context.
- \`version\`: Optimistic locking sequence number.

### \`claims_db.audit_records\` Schema
The \`claims-service\` maintains a dedicated audit table:
\`\`\`sql
CREATE TABLE audit_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'SUBMIT', 'VALIDATE', 'ADJUDICATE', 'APPROVE', 'REJECT'
    performed_by VARCHAR(100) NOT NULL,
    previous_state TEXT,
    new_state TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
\`\`\`

## 14.2 Spring AOP Audit & Execution Profiler
\`\`\`java
@Aspect
@Component
@Slf4j
public class ServiceAuditAspect {

    @Around("execution(* com.hims..service..*(..))")
    public Object auditServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                          SecurityContextHolder.getContext().getAuthentication().getName() : "ANONYMOUS";

        log.info("[AUDIT-START] User='{}', Method='{}'", username, methodName);
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AUDIT-SUCCESS] User='{}', Method='{}', Duration={}ms", username, methodName, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AUDIT-FAILURE] User='{}', Method='{}', Duration={}ms, Error='{}'", username, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}
\`\`\`

## 14.3 Distributed Correlation Tracing with MDC
When requests arrive at the \`api-gateway\`, a unique \`X-Correlation-Id\` UUID is generated and attached to request headers. Downstream microservices register this ID in Logback's **Mapped Diagnostic Context (MDC)**:
\`\`\`
2026-09-30 10:15:30.120 [http-nio-8092-exec-1] [corr-78a9c2b4] INFO  com.hims.claims.ClaimController - Received claim submission for POL-2026-0089
2026-09-30 10:15:30.145 [http-nio-8092-exec-1] [corr-78a9c2b4] INFO  com.hims.claims.PolicyClient - Calling Policy Service for verification
\`\`\`
This enables DevOps and developers to grep the entire cluster for \`corr-78a9c2b4\` to trace a single transaction across Gateway, Policy, Provider, Claims, and Kafka logs.

---
`;
  },

  getSection15: function() {
    return `
# 15. OPENFEIGN COMMUNICATION

## 15.1 Declarative Synchronous RPC via OpenFeign
Spring Cloud OpenFeign eliminates repetitive HTTP boilerplate code by allowing developers to define declarative Java interfaces annotated with Spring MVC mappings. In HIMS, OpenFeign works hand-in-hand with Netflix Eureka for client-side load balancing.

\`\`\`
+-----------------------------------------------------------------------------------+
|                        COMPLETE OPENFEIGN CLIENT DIRECTORY                        |
+-----------------------------------------------------------------------------------+
| Calling Microservice   | Feign Client Interface       | Target Microservice       |
+------------------------+------------------------------+---------------------------+
| claims-service         | PolicyServiceClient          | policy-service (8088)     |
| claims-service         | ProviderServiceClient        | provider-service (8091)   |
| policy-service         | CustomerClient               | customer-service (8083)   |
| policy-service         | ProductClient                | product-plan-service(8084)|
| policy-service         | QuotationClient              | quotation-service (8085)  |
| policy-service         | PremiumClient                | premium-service (8089)    |
| policy-service         | PaymentClient                | payment-service (8090)    |
| premium-service        | ProductPlanClient            | product-plan-service(8084)|
| premium-service        | QuotationClient              | quotation-service (8085)  |
| quotation-service      | CustomerClient               | customer-service (8083)   |
| quotation-service      | ProductClient                | product-plan-service(8084)|
| risk-service           | CustomerClient               | customer-service (8083)   |
| risk-service           | QuotationClient              | quotation-service (8085)  |
| underwriting-service   | QuotationClient              | quotation-service (8085)  |
| underwriting-service   | RiskAssessmentClient         | risk-service (8086)       |
+-----------------------------------------------------------------------------------+
\`\`\`

## 15.2 Representative Feign Client Definitions

### 1. \`claims-service\` -> \`PolicyServiceClient\`
\`\`\`java
@FeignClient(name = "policy-service", configuration = FeignClientConfiguration.class)
public interface PolicyServiceClient {

    @GetMapping("/api/policies/number/{policyNumber}")
    ApiResponse<PolicyDto> getPolicyByNumber(@PathVariable("policyNumber") String policyNumber);

    @GetMapping("/api/policies/{id}/eligibility")
    ApiResponse<PolicyEligibilityDto> checkEligibility(@PathVariable("id") Long id, @RequestParam("date") String date);
}
\`\`\`

### 2. \`claims-service\` -> \`ProviderServiceClient\`
\`\`\`java
@FeignClient(name = "provider-service", configuration = FeignClientConfiguration.class)
public interface ProviderServiceClient {

    @GetMapping("/api/providers/code/{providerCode}")
    ApiResponse<ProviderDto> getProviderByCode(@PathVariable("providerCode") String providerCode);
}
\`\`\`

### 3. \`quotation-service\` -> \`CustomerClient\` & \`ProductClient\`
\`\`\`java
@FeignClient(name = "customer-service")
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    ApiResponse<CustomerDto> getCustomerById(@PathVariable("id") Long id);
}

@FeignClient(name = "product-plan-service")
public interface ProductClient {
    @GetMapping("/api/plans/{id}")
    ApiResponse<PlanDto> getPlanById(@PathVariable("id") Long id);
}
\`\`\`

## 15.3 Global Feign Configuration & Error Decoding
To prevent uncaught 500 runtime exceptions when a downstream service returns a 404 or 400, HIMS implements a custom \`ErrorDecoder\`:
\`\`\`java
public class CustomFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new ResourceNotFoundException("Remote entity not found via Feign call: " + methodKey);
        }
        if (response.status() == 400) {
            return new BadRequestException("Remote service reported bad request: " + methodKey);
        }
        return new ServiceUnavailableException("Downstream service communication failure: " + response.status());
    }
}
\`\`\`

---
`;
  },

  getSection16: function() {
    return `
# 16. FRONTEND ARCHITECTURE

## 16.1 Angular 17 Application Structure
The HIMS client is a high-performance Single Page Application (SPA) built with Angular 17 and TypeScript, located in \`frontend/hims-ui\`. It employs a 3-tier modular directory layout:

\`\`\`
src/app/
├── core/                   # Singleton services, interceptors, authentication guards
│   ├── auth/               # AuthService, TokenStorageService, AuthGuard, RoleGuard
│   ├── interceptors/       # JwtInterceptor, ErrorInterceptor
│   ├── models/             # Strongly-typed TypeScript interfaces matching Java DTOs
│   └── services/           # ApiService, NotificationService, UtilityService
├── features/               # Domain-specific feature modules with routed views
│   ├── auth/               # Login, Register, Forgot Password
│   ├── customer/           # Customer registration, Profile, KYC upload
│   ├── product-plan/       # Product catalog, Plan comparison cards
│   ├── quotation/          # Quote wizard, actuarial loading preview
│   ├── risk/               # Medical disclosures, BMI calculator, risk score card
│   ├── underwriting/       # Underwriter case approval dashboard & loading adjuster
│   ├── policy/             # Policy list, Policy issuance wizard, e-Card download
│   ├── premium/            # Installment schedules, Due date timeline, receipt viewer
│   ├── payment/            # Mock gateway payment checkout modal, receipt view
│   ├── provider/           # Hospital directory search, In-network map/filter
│   ├── claim/              # Claim submission form, 6-stage status tracker, EOB viewer
│   ├── document/           # Document repository, drag-and-drop file uploader
│   ├── notification/       # Notification bell, alert center
│   ├── report/             # Executive analytics charts, claim settlement KPIs
│   └── user-management/    # Admin user provisioning & role assignment
└── shared/                 # Reusable UI components, pipes, directives, modals
\`\`\`

## 16.2 HTTP Interceptors Pipeline

### 1. \`JwtInterceptor\`
Automatically intercepts every outgoing HTTP request to \`/api/**\` and injects the \`Authorization: Bearer <token>\` header retrieved from \`TokenStorageService\`.
\`\`\`typescript
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private tokenStorage: TokenStorageService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.tokenStorage.getToken();
    if (token) {
      request = request.clone({
        setHeaders: { Authorization: \`Bearer \${token}\` }
      });
    }
    return next.handle(request);
  }
}
\`\`\`

### 2. \`ErrorInterceptor\`
Catches backend error responses globally. If an HTTP 401 is received, it clears the token storage and routes the user to \`/auth/login\`. If an HTTP 400 validation error occurs, it parses the field errors and renders visual alerts.

## 16.3 Route Guards Architecture
- **\`AuthGuard\`**: Protects all private routes. Checks if a valid, unexpired token exists in local storage.
- **\`RoleGuard\`**: Verifies that the authenticated user possesses the specific role required by the route configuration:
\`\`\`typescript
const routes: Routes = [
  {
    path: 'underwriting',
    component: UnderwritingDashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['ROLE_UNDERWRITER', 'ROLE_ADMIN'] }
  },
  {
    path: 'claims/adjudication',
    component: ClaimAdjudicationComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { expectedRoles: ['ROLE_CLAIMS_OFFICER', 'ROLE_ADMIN'] }
  }
];
\`\`\`

---
`;
  }
};
