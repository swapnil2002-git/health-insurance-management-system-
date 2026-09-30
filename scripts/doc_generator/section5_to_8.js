// Section 5 to 8 module
module.exports = {
  getSection5: function() {
    return `
# 5. COMPLETE BUSINESS FLOW

The HIMS platform coordinates two primary business workflows:
1. **The Policy Acquisition & Issuance Journey**
2. **The Healthcare Claim Adjudication & Settlement Journey**

---

## 5.1 Journey 1: Policy Acquisition & Activation Flow

The end-to-end lifecycle from customer onboarding to policy activation:

\`\`\`
[1. Customer Registration]  ──► POST /api/customers ──► (customer_db)
            │
            ▼
[2. Plan Discovery]         ──► GET /api/plans ──► (product_db)
            │
            ▼
[3. Quotation Request]      ──► POST /api/quotes ──► OpenFeign (Customer, Product) ──► (quotation_db)
            │
            ▼
[4. Medical Disclosures]    ──► POST /api/risk-assessments ──► Risk Scoring Algorithm ──► (risk_db)
            │
            ▼
[5. Underwriting Review]    ──► POST /api/underwriting/evaluate ──► OpenFeign (Risk, Quote) ──► (underwriting_db)
            │
            ▼
[6. Policy Issuance]        ──► POST /api/policies/issue ──► Writes Policy & OutboxEvent ──► (policy_db)
            │                                                      │
            │                                                      ▼ (PolicyOutboxPublisher)
            │                                                 Kafka: "policy-events"
            │                                                      │
            ▼                                                      ▼
[7. Premium Schedule Saga]  ◄──────────────────────────────── PremiumService (PolicyIssuedConsumer)
            │                                                      │
            │                                                      ▼ (Writes Installments)
            ▼                                                 (premium_db)
[8. Premium Payment]        ──► POST /api/payments/process ──► Simulates Gateway & Idempotency ──► (payment_db)
            │                                                      │
            │                                                      ▼ (PaymentOutboxPublisher)
            │                                                 Kafka: "premium-events"
            ▼                                                      │
[9. Policy Activation]      ◄──────────────────────────────── PolicyService (PolicyActivationConsumer)
                                                                   │
                                                                   ▼
                                                              Policy Status -> ACTIVE
\`\`\`

### Step-by-Step Policy Flow Specification

| Step | Business Action | Service Involved | API Endpoint / Trigger | Databases & Tables Affected | Protocol / Mechanism | Event Emitted | Next Step |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Customer Onboarding | \`customer-service\` | \`POST /api/customers\` | \`customer_db\`: \`customers\`, \`customer_addresses\`, \`customer_nominees\` | REST (HTTP) | \`CustomerCreatedEvent\` | Customer browses plans |
| **2** | Plan Selection | \`product-plan-service\`| \`GET /api/plans?productId=1\` | \`product_db\`: \`plans\`, \`plan_benefits\` | REST (HTTP) | None (Read-only) | User selects plan, inputs coverage sum |
| **3** | Quote Calculation | \`quotation-service\` | \`POST /api/quotes\` | \`quotation_db\`: \`quotes\`, \`quote_members\` | REST -> OpenFeign to Customer & Product | \`QuoteGeneratedEvent\` | Quote valid for 30 days; user proceeds to health disclosure |
| **4** | Risk Assessment | \`risk-service\` | \`POST /api/risk-assessments\`| \`risk_db\`: \`risk_assessments\`, \`risk_factors\` | REST -> OpenFeign to Quotation | \`RiskAssessmentCompletedEvent\` | Risk score calculated (0-100); sent to Underwriting |
| **5** | Underwriting Decision | \`underwriting-service\`| \`POST /api/underwriting/evaluate\` | \`underwriting_db\`: \`underwriting_cases\` | REST -> OpenFeign to Risk & Quotation | \`UnderwritingApprovedEvent\` | If score <= 30: Auto-approved; else sent to underwriter queue |
| **6** | Policy Issuance | \`policy-service\` | \`POST /api/policies/issue\` | \`policy_db\`: \`policies\`, \`policy_members\`, \`outbox_events\` | REST (ACID Transaction) | \`PolicyIssuedEvent\` (Outbox -> Kafka \`policy-events\`) | Policy created in \`ISSUED\` status; triggers Saga |
| **7** | Premium Scheduling | \`premium-service\` | \`@KafkaListener(policy-events)\` | \`premium_db\`: \`premium_schedules\`, \`installments\` | Asynchronous Kafka Event | None (or \`PremiumScheduleFailed\` on error) | Premium installments generated; first payment due |
| **8** | Initial Payment | \`payment-service\` | \`POST /api/payments/process\` | \`payment_db\`: \`payments\`, \`idempotency_records\`, \`outbox_events\` | REST with \`X-Idempotency-Key\` | \`PremiumPaidEvent\` (Outbox -> Kafka \`premium-events\`) | Payment confirmed |
| **9** | Policy Activation | \`policy-service\` | \`@KafkaListener(premium-events)\` | \`policy_db\`: \`policies\` (\`status='ACTIVE'\`) | Asynchronous Kafka Event | \`PolicyActivatedEvent\` | Policy is now in-force; member can access hospital services |

---

## 5.2 Journey 2: Claim Adjudication & Settlement Flow

The end-to-end workflow when a policyholder or hospital files a claim:

\`\`\`
[1. Claim Submission] ──► POST /api/claims ──► Idempotency Check & Persist ──► (claims_db: claims)
           │
           ▼
[2. Automated 6-Stage Adjudication Engine]
   ├─ Stage 1: Completeness Check (dates, non-empty bills)
   ├─ Stage 2: Synchronous OpenFeign -> PolicyService.getPolicyByNumber(policyNumber)
   │           └── Verify Policy is ACTIVE, current date within [start_date, end_date]
   ├─ Stage 3: Synchronous OpenFeign -> ProviderService.getProviderByCode(providerCode)
   │           └── Verify Provider license is VALID and network status is IN_NETWORK
   ├─ Stage 4: Benefit & Waiting Period Check (ICD-10 vs policy benefit clauses)
   ├─ Stage 5: Deductible & Co-pay Computation:
   │           Net Approved = (Billed - NonCovered - Deductible) * (1 - Copay%)
   └─ Stage 6: Final Adjudication State -> APPROVED or REJECTED
           │
           ▼
[3. Generate Explanation of Benefits (EOB)] ──► Persist in claims_db (explanation_of_benefits)
           │
           ▼
[4. Transactional Outbox Relay] ──► Writes ClaimApprovedEvent to claims_db: outbox_events
           │
           ▼ (ClaimOutboxPublisher Relay)
    Kafka Topic: "claim-events"
           │
           ├───────────────────────────────┐
           ▼                               ▼
[5. Notification Service]       [6. Reporting Service]
    Dispatches SMS / Email          Updates settlement analytics & loss ratio
\`\`\`

### Step-by-Step Claim Adjudication Specification

| Stage / Step | Business Action | Implementing Class / Service | Dependency & Verification | Outcome / Tables Updated |
| :--- | :--- | :--- | :--- | :--- |
| **Submission** | Claim received via UI / Hospital portal | \`ClaimController\` (\`claims-service\`) | Validates \`IdempotencyRecord\` to prevent duplicate submissions | Persists in \`claims\`, \`claim_services\`, \`claim_diagnoses\` with status \`SUBMITTED\` |
| **Stage 1** | Format & Completeness Validation | \`ClaimValidationService\` | Verifies mandatory fields, positive billed amounts, admission <= discharge | Records check in \`claim_validations\`; failure rejects claim with \`INVALID_FORMAT\` |
| **Stage 2** | Policy Eligibility Verification | \`PolicyServiceClient\` (OpenFeign) | Calls \`policy-service\` \`/api/policies/number/{num}\`; verifies status is \`ACTIVE\` and coverage period active | If policy lapsed, marked \`POLICY_INACTIVE\`; else proceeds to Stage 3 |
| **Stage 3** | Provider Network Verification | \`ProviderServiceClient\` (OpenFeign) | Calls \`provider-service\` \`/api/providers/code/{code}\`; verifies license is active | If suspended or invalid license, marked \`PROVIDER_NOT_ACCREDITED\` |
| **Stage 4** | Benefit & Waiting Period Check | \`BenefitAdjudicationEngine\` | Evaluates primary ICD-10 code against pre-existing disease waiting period clauses | If treatment falls in waiting period, disallows item |
| **Stage 5** | Deductible & Copay Math | \`ClaimAdjudicationService\` | Subtracts non-covered expenses, applies remaining policy deductible, applies co-payment percentage | Generates \`claim_adjudications\` record with \`net_approved\` amount |
| **Stage 6** | EOB & Settlement Generation | \`EobService\` & \`ClaimPaymentService\` | Creates breakdown of insurer vs patient responsibility | Inserts into \`explanation_of_benefits\` and \`claim_payments\` |
| **Outbox Relay**| Asynchronous Event Dispatch | \`ClaimOutboxPublisher\` | Polls \`PENDING\` outbox records; publishes to Kafka \`claim-events\` topic | Updates outbox row to \`PUBLISHED\`; triggers Notification and Reporting updates |

---
`;
  },

  getSection6: function() {
    return `
# 6. DATABASE ARCHITECTURE

## 6.1 Database-per-Service Architecture Rationale
A foundational architectural mandate of HIMS is **Database-per-Service Isolation**:
- Every microservice connects strictly to its own dedicated MySQL database schema.
- **Zero Cross-Database Joins**: No service is permitted to execute SQL joins across service boundaries.
- **No Shared Tables**: Shared database entities are strictly prohibited. Information exchange between domains occurs exclusively via typed REST/OpenFeign requests or Kafka event messages.
- **Independent Schema Evolution**: The \`claims-service\` database schema can be altered, migrated, or optimized with indexes without risking schema lock contention or regression in the \`policy-service\` or \`customer-service\`.
- **Fault Containment**: If the \`reporting_db\` or \`document_db\` experiences high I/O saturation or downtime, critical customer onboarding and claim submission transactions continue uninterrupted.

---

## 6.2 Complete Microservice Database Schema Reference

### 1. \`identity_db\` (Identity Service)
- **\`users\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`username\` VARCHAR(50) NOT NULL UNIQUE
  - \`email\` VARCHAR(100) NOT NULL UNIQUE
  - \`password\` VARCHAR(255) NOT NULL (BCrypt hash)
  - \`first_name\` VARCHAR(50) NOT NULL
  - \`last_name\` VARCHAR(50) NOT NULL
  - \`enabled\` BOOLEAN DEFAULT TRUE
  - \`created_at\` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  - \`updated_at\` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- **\`roles\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`name\` VARCHAR(50) NOT NULL UNIQUE (e.g. \`ROLE_CUSTOMER\`, \`ROLE_AGENT\`, \`ROLE_UNDERWRITER\`, \`ROLE_CLAIMS_OFFICER\`, \`ROLE_ADMIN\`)
- **\`user_roles\`**:
  - \`user_id\` BIGINT NOT NULL, FK -> \`users(id)\`
  - \`role_id\` BIGINT NOT NULL, FK -> \`roles(id)\`
  - PRIMARY KEY (\`user_id\`, \`role_id\`)

### 2. \`customer_db\` (Customer Service)
- **\`customers\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`customer_code\` VARCHAR(50) NOT NULL UNIQUE (e.g. \`CUST-2026-0001\`)
  - \`first_name\` VARCHAR(50) NOT NULL
  - \`last_name\` VARCHAR(50) NOT NULL
  - \`email\` VARCHAR(100) NOT NULL UNIQUE
  - \`phone\` VARCHAR(15) NOT NULL
  - \`date_of_birth\` DATE NOT NULL
  - \`gender\` VARCHAR(10) NOT NULL
  - \`pan_number\` VARCHAR(10)
  - \`aadhaar_number\` VARCHAR(12)
  - \`kyc_status\` VARCHAR(20) NOT NULL DEFAULT 'PENDING'
  - \`created_at\`, \`updated_at\` TIMESTAMP
- **\`customer_addresses\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`customer_id\` BIGINT NOT NULL, FK -> \`customers(id)\`
  - \`address_type\` VARCHAR(20) NOT NULL
  - \`street\` VARCHAR(255) NOT NULL
  - \`city\` VARCHAR(100) NOT NULL
  - \`state\` VARCHAR(100) NOT NULL
  - \`postal_code\` VARCHAR(10) NOT NULL
  - \`country\` VARCHAR(50) NOT NULL DEFAULT 'India'
- **\`customer_nominees\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`customer_id\` BIGINT NOT NULL, FK -> \`customers(id)\`
  - \`nominee_name\` VARCHAR(100) NOT NULL
  - \`relationship\` VARCHAR(50) NOT NULL
  - \`date_of_birth\` DATE NOT NULL
  - \`allocation_percentage\` DECIMAL(5,2) NOT NULL

### 3. \`product_db\` (Product & Plan Service)
- **\`products\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`product_code\` VARCHAR(50) NOT NULL UNIQUE
  - \`product_name\` VARCHAR(100) NOT NULL
  - \`product_type\` VARCHAR(50) NOT NULL
  - \`description\` TEXT
  - \`is_active\` BOOLEAN DEFAULT TRUE
- **\`plans\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`product_id\` BIGINT NOT NULL, FK -> \`products(id)\`
  - \`plan_code\` VARCHAR(50) NOT NULL UNIQUE
  - \`plan_name\` VARCHAR(100) NOT NULL
  - \`sum_insured\` DECIMAL(15,2) NOT NULL
  - \`base_premium\` DECIMAL(12,2) NOT NULL
  - \`min_age\` INT NOT NULL
  - \`max_age\` INT NOT NULL
  - \`is_active\` BOOLEAN DEFAULT TRUE
- **\`plan_benefits\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`plan_id\` BIGINT NOT NULL, FK -> \`plans(id)\`
  - \`benefit_name\` VARCHAR(100) NOT NULL
  - \`benefit_code\` VARCHAR(50) NOT NULL
  - \`limit_amount\` DECIMAL(15,2)
  - \`waiting_period_days\` INT DEFAULT 0
  - \`copay_percentage\` DECIMAL(5,2) DEFAULT 0.00

### 4. \`quotation_db\` (Quotation Service)
- **\`quotes\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`quote_number\` VARCHAR(50) NOT NULL UNIQUE
  - \`customer_id\` BIGINT NOT NULL
  - \`plan_id\` BIGINT NOT NULL
  - \`sum_insured\` DECIMAL(15,2) NOT NULL
  - \`base_premium\` DECIMAL(12,2) NOT NULL
  - \`tax_amount\` DECIMAL(12,2) NOT NULL
  - \`total_premium\` DECIMAL(12,2) NOT NULL
  - \`quote_status\` VARCHAR(20) NOT NULL DEFAULT 'GENERATED'
  - \`expiry_date\` DATE NOT NULL
  - \`created_at\` TIMESTAMP
- **\`quote_members\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`quote_id\` BIGINT NOT NULL, FK -> \`quotes(id)\`
  - \`relationship\` VARCHAR(30) NOT NULL
  - \`age\` INT NOT NULL
  - \`gender\` VARCHAR(10) NOT NULL
  - \`tobacco_user\` BOOLEAN DEFAULT FALSE

### 5. \`risk_db\` (Risk Assessment Service)
- **\`risk_assessments\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`assessment_number\` VARCHAR(50) NOT NULL UNIQUE
  - \`quote_id\` BIGINT NOT NULL
  - \`customer_id\` BIGINT NOT NULL
  - \`risk_score\` INT NOT NULL (0 to 100)
  - \`risk_grade\` VARCHAR(20) NOT NULL (\`LOW\`, \`MEDIUM\`, \`HIGH\`, \`DECLINED\`)
  - \`recommended_loading_pct\` DECIMAL(5,2) DEFAULT 0.00
  - \`assessment_status\` VARCHAR(30) NOT NULL
  - \`created_at\` TIMESTAMP
- **\`risk_factors\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`assessment_id\` BIGINT NOT NULL, FK -> \`risk_assessments(id)\`
  - \`factor_name\` VARCHAR(100) NOT NULL
  - \`factor_type\` VARCHAR(50) NOT NULL
  - \`score_impact\` INT NOT NULL
  - \`details\` VARCHAR(255)

### 6. \`underwriting_db\` (Underwriting Service)
- **\`underwriting_cases\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`case_number\` VARCHAR(50) NOT NULL UNIQUE
  - \`quote_id\` BIGINT NOT NULL
  - \`risk_assessment_id\` BIGINT NOT NULL
  - \`status\` VARCHAR(30) NOT NULL (\`APPROVED\`, \`REJECTED\`, \`PENDING_REVIEW\`, \`REFERRED\`)
  - \`approved_by\` VARCHAR(100)
  - \`premium_loading_percentage\` DECIMAL(5,2) DEFAULT 0.00
  - \`special_conditions\` TEXT
  - \`rejection_reason\` VARCHAR(255)
  - \`decided_at\` TIMESTAMP

### 7. \`policy_db\` (Policy Service)
- **\`policies\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`policy_number\` VARCHAR(50) NOT NULL UNIQUE
  - \`customer_id\` BIGINT NOT NULL
  - \`plan_id\` BIGINT NOT NULL
  - \`quote_id\` BIGINT NOT NULL
  - \`underwriting_case_id\` BIGINT NOT NULL
  - \`status\` VARCHAR(30) NOT NULL (\`DRAFT\`, \`ISSUED\`, \`ACTIVE\`, \`EXPIRED\`, \`CANCELLED\`)
  - \`start_date\` DATE NOT NULL
  - \`end_date\` DATE NOT NULL
  - \`sum_insured\` DECIMAL(15,2) NOT NULL
  - \`total_premium\` DECIMAL(12,2) NOT NULL
  - \`version\` INT NOT NULL DEFAULT 0 (Optimistic Lock)
  - \`created_at\`, \`updated_at\` TIMESTAMP
- **\`policy_members\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`policy_id\` BIGINT NOT NULL, FK -> \`policies(id)\`
  - \`member_name\` VARCHAR(100) NOT NULL
  - \`relationship\` VARCHAR(30) NOT NULL
  - \`date_of_birth\` DATE NOT NULL
  - \`sum_insured_share\` DECIMAL(15,2)
- **\`outbox_events\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`aggregate_type\` VARCHAR(50) NOT NULL
  - \`aggregate_id\` VARCHAR(50) NOT NULL
  - \`event_type\` VARCHAR(100) NOT NULL
  - \`payload\` TEXT NOT NULL
  - \`status\` VARCHAR(20) NOT NULL DEFAULT 'PENDING'
  - \`retry_count\` INT DEFAULT 0
  - \`created_at\` TIMESTAMP

### 8. \`premium_db\` (Premium Service)
- **\`premium_schedules\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`policy_id\` BIGINT NOT NULL UNIQUE
  - \`policy_number\` VARCHAR(50) NOT NULL
  - \`payment_frequency\` VARCHAR(20) NOT NULL
  - \`total_installments\` INT NOT NULL
  - \`total_amount\` DECIMAL(12,2) NOT NULL
  - \`paid_amount\` DECIMAL(12,2) DEFAULT 0.00
  - \`balance_amount\` DECIMAL(12,2) NOT NULL
  - \`status\` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
- **\`installments\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`schedule_id\` BIGINT NOT NULL, FK -> \`premium_schedules(id)\`
  - \`installment_number\` INT NOT NULL
  - \`due_date\` DATE NOT NULL
  - \`amount\` DECIMAL(12,2) NOT NULL
  - \`grace_period_end_date\` DATE NOT NULL
  - \`status\` VARCHAR(20) NOT NULL DEFAULT 'PENDING'
  - \`paid_at\` TIMESTAMP NULL

### 9. \`payment_db\` (Payment Service)
- **\`payments\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`transaction_reference\` VARCHAR(100) NOT NULL UNIQUE
  - \`policy_id\` BIGINT NOT NULL
  - \`installment_id\` BIGINT NULL
  - \`amount\` DECIMAL(12,2) NOT NULL
  - \`payment_method\` VARCHAR(30) NOT NULL
  - \`payment_status\` VARCHAR(30) NOT NULL
  - \`gateway_response_code\` VARCHAR(50)
  - \`created_at\`, \`updated_at\` TIMESTAMP
- **\`idempotency_records\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`idempotency_key\` VARCHAR(100) NOT NULL UNIQUE
  - \`request_hash\` VARCHAR(64) NOT NULL
  - \`response_body\` TEXT
  - \`status\` VARCHAR(20) NOT NULL
  - \`created_at\` TIMESTAMP
- **\`outbox_events\`**: Transactional outbox table for payment events.

### 10. \`provider_db\` (Provider Service)
- **\`providers\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`provider_code\` VARCHAR(50) NOT NULL UNIQUE
  - \`provider_name\` VARCHAR(150) NOT NULL
  - \`provider_type\` VARCHAR(50) NOT NULL
  - \`license_number\` VARCHAR(100) NOT NULL UNIQUE
  - \`network_status\` VARCHAR(30) NOT NULL DEFAULT 'IN_NETWORK'
  - \`tier\` VARCHAR(20) NOT NULL DEFAULT 'TIER_1'
  - \`discount_percentage\` DECIMAL(5,2) DEFAULT 0.00
  - \`is_active\` BOOLEAN DEFAULT TRUE
  - \`created_at\` TIMESTAMP
- **\`provider_departments\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`provider_id\` BIGINT NOT NULL, FK -> \`providers(id)\`
  - \`department_name\` VARCHAR(100) NOT NULL
  - \`contact_phone\` VARCHAR(20)

### 11. \`claims_db\` (Claims Service - 11 Tables)
- **\`claims\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`claim_number\` VARCHAR(50) NOT NULL UNIQUE
  - \`policy_number\` VARCHAR(50) NOT NULL
  - \`customer_id\` BIGINT NOT NULL
  - \`provider_code\` VARCHAR(50) NOT NULL
  - \`claim_type\` VARCHAR(30) NOT NULL (\`CASHLESS\`, \`REIMBURSEMENT\`)
  - \`admission_date\` DATE NOT NULL
  - \`discharge_date\` DATE NOT NULL
  - \`claimed_amount\` DECIMAL(15,2) NOT NULL
  - \`approved_amount\` DECIMAL(15,2) DEFAULT 0.00
  - \`deductible_amount\` DECIMAL(15,2) DEFAULT 0.00
  - \`copay_amount\` DECIMAL(15,2) DEFAULT 0.00
  - \`settlement_status\` VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED'
  - \`rejection_reason\` VARCHAR(255)
  - \`created_at\`, \`updated_at\` TIMESTAMP
- **\`claim_diagnoses\`**: \`id\`, \`claim_id\` (FK), \`icd10_code\`, \`description\`, \`is_primary\`
- **\`claim_services\`**: \`id\`, \`claim_id\` (FK), \`service_date\`, \`service_code\`, \`service_description\`, \`billed_amount\`, \`allowed_amount\`
- **\`claim_documents\`**: \`id\`, \`claim_id\` (FK), \`document_id\`, \`document_type\`, \`verified\`
- **\`claim_validations\`**: \`id\`, \`claim_id\` (FK), \`validation_stage\`, \`rule_name\`, \`passed\`, \`details\`
- **\`claim_adjudications\`**: \`id\`, \`claim_id\` (FK), \`total_billed\`, \`disallowed_amount\`, \`deductible_applied\`, \`copay_applied\`, \`net_approved\`, \`adjudicated_by\`, \`adjudicated_at\`
- **\`explanation_of_benefits\`**: \`id\`, \`claim_id\` (FK), \`eob_number\` UNIQUE, \`patient_responsibility\`, \`insurer_paid\`, \`notes\`, \`generated_at\`
- **\`claim_payments\`**: \`id\`, \`claim_id\` (FK), \`payment_reference\`, \`payment_amount\`, \`disbursement_status\`, \`disbursed_at\`
- **\`idempotency_records\`**: Deduplication store for claims
- **\`outbox_events\`**: Outbox table for \`claim-events\`
- **\`audit_records\`**: Entity audit change log

### 12. \`document_db\` (Document Service)
- **\`documents\`**:
  - \`id\` BIGINT AUTO_INCREMENT PRIMARY KEY
  - \`document_code\` VARCHAR(50) NOT NULL UNIQUE
  - \`file_name\` VARCHAR(255) NOT NULL
  - \`file_type\` VARCHAR(100) NOT NULL
  - \`file_size\` BIGINT NOT NULL
  - \`storage_path\` VARCHAR(500) NOT NULL
  - \`entity_type\` VARCHAR(50) NOT NULL
  - \`entity_id\` BIGINT NOT NULL
  - \`uploaded_at\` TIMESTAMP

### 13. \`notification_db\` & \`reporting_db\`
- \`notification_db.notifications\`: \`id\`, \`recipient_email\`, \`recipient_phone\`, \`notification_type\`, \`subject\`, \`message_body\`, \`event_source\`, \`delivery_status\`, \`sent_at\`.
- \`reporting_db.daily_kpi_summaries\`: \`id\`, \`summary_date\` UNIQUE, \`total_policies_issued\`, \`total_premium_collected\`, \`total_claims_submitted\`, \`total_claims_approved\`, \`total_claims_amount_paid\`, \`loss_ratio\`.
- \`reporting_db.claim_analytics\`: \`id\`, \`claim_number\`, \`policy_number\`, \`provider_code\`, \`claimed_amount\`, \`approved_amount\`, \`adjudication_time_seconds\`.

---
`;
  },

  getSection7: function() {
    return `
# 7. REST API ARCHITECTURE

## 7.1 REST Principles & Engineering Standards
All HIMS microservices adhere strictly to REST architectural constraints:
1. **Resource Identification via URIs**: Plural nouns represent collections (e.g. \`/api/policies\`, \`/api/claims\`, \`/api/customers\`).
2. **Standard HTTP Verbs**:
   - \`GET\`: Safe, idempotent read operations.
   - \`POST\`: Resource creation and command execution.
   - \`PUT\`: Idempotent full state updates or lifecycle state transitions (e.g. \`/approve\`).
   - \`DELETE\`: Resource removal or soft-deletion.
3. **Standard HTTP Status Codes**:
   - \`200 OK\`: Successful retrieval or modification.
   - \`201 Created\`: Successful resource creation (with \`Location\` header or created object body).
   - \`400 Bad Request\`: JSR-380 validation failure or malformed payload.
   - \`401 Unauthorized\`: Missing, expired, or invalid JWT token.
   - \`403 Forbidden\`: Authenticated user lacks required role/authority.
   - \`404 Not Found\`: Resource identifier does not exist.
   - \`409 Conflict\`: Duplicate business key or optimistic locking version conflict.
   - \`500 Internal Server Error\`: Unhandled infrastructure failure.

## 7.2 Standardized API Response Wrapper
All endpoints return an immutable, standardized response envelope:
\`\`\`json
{
  "success": true,
  "message": "Policy issued successfully",
  "data": {
    "id": 1042,
    "policyNumber": "POL-2026-0089",
    "status": "ISSUED",
    "sumInsured": 500000.00,
    "totalPremium": 14500.00
  },
  "timestamp": "2026-09-30T10:15:30Z"
}
\`\`\`

## 7.3 Global Exception Handling & Error Envelope
Global exceptions are intercepted by \`@RestControllerAdvice\` classes in each service, normalizing errors into a secure schema:
\`\`\`json
{
  "success": false,
  "errorCode": "VALIDATION_FAILED",
  "message": "Input validation failed for 2 field(s)",
  "errors": {
    "panNumber": "PAN number must match uppercase 10-character pattern [A-Z]{5}[0-9]{4}[A-Z]{1}",
    "phone": "Phone number must be exactly 10 digits"
  },
  "timestamp": "2026-09-30T10:15:32Z",
  "path": "/api/customers"
}
\`\`\`

## 7.4 Representative API Implementation Examples

### Example 1: Issue Policy API (\`policy-service\`)
- **Verb & Path**: \`POST /api/policies/issue\`
- **Authorization**: \`hasAnyRole('ROLE_UNDERWRITER', 'ROLE_AGENT', 'ROLE_ADMIN')\`
- **Request Body**:
\`\`\`json
{
  "quoteId": 204,
  "underwritingCaseId": 102,
  "paymentFrequency": "ANNUAL"
}
\`\`\`
- **Response (\`201 Created\`)**:
\`\`\`json
{
  "success": true,
  "message": "Policy successfully issued and queued for premium schedule generation",
  "data": {
    "policyId": 89,
    "policyNumber": "POL-2026-0089",
    "status": "ISSUED",
    "startDate": "2026-10-01",
    "endDate": "2027-09-30",
    "sumInsured": 500000.00,
    "totalPremium": 14500.00
  }
}
\`\`\`

### Example 2: Submit Healthcare Claim (\`claims-service\`)
- **Verb & Path**: \`POST /api/claims\`
- **Headers**: \`X-Idempotency-Key: 7b8e1f0e-3c9a-412d-b152-32a893c52a01\`
- **Request Body**:
\`\`\`json
{
  "policyNumber": "POL-2026-0089",
  "customerId": 15,
  "providerCode": "HOSP-MAX-001",
  "claimType": "CASHLESS",
  "admissionDate": "2026-10-15",
  "dischargeDate": "2026-10-18",
  "claimedAmount": 45000.00,
  "diagnoses": [
    {
      "icd10Code": "J18.9",
      "description": "Pneumonia, unspecified organism",
      "isPrimary": true
    }
  ],
  "services": [
    {
      "serviceDate": "2026-10-15",
      "serviceCode": "ICU-BED",
      "serviceDescription": "Intensive Care Bed Charge",
      "billedAmount": 30000.00
    },
    {
      "serviceDate": "2026-10-16",
      "serviceCode": "MED-ANTIBIOTIC",
      "serviceDescription": "Intravenous Antibiotics",
      "billedAmount": 15000.00
    }
  ]
}
\`\`\`
- **Response (\`201 Created\`)**:
\`\`\`json
{
  "success": true,
  "message": "Claim submitted and automated adjudication initiated",
  "data": {
    "claimId": 501,
    "claimNumber": "CLM-2026-0501",
    "settlementStatus": "SUBMITTED",
    "claimedAmount": 45000.00
  }
}
\`\`\`

---
`;
  },

  getSection8: function() {
    return `
# 8. KAFKA EVENT-DRIVEN ARCHITECTURE

## 8.1 Why Apache Kafka in HIMS?
In an enterprise insurance platform, tight synchronous coupling across all services creates catastrophic brittleness:
1. **Temporal Decoupling**: If the \`notification-service\` is restarting for an upgrade, policy issuance must NOT fail. The event sits durably in the \`policy-events\` topic until the consumer comes back online.
2. **Transactional Outbox Guarantee**: By writing domain events to a local relational \`outbox_events\` table within the same database transaction as the business entity, HIMS guarantees **At-Least-Once Delivery** with zero data loss even during sudden power failure.
3. **High Throughput & Replayability**: Apache Kafka's partitioned commit log allows analytical consumers in \`reporting-service\` to reprocess event streams from offset 0 to recompute historical metrics.

## 8.2 Standard HIMS Event Envelope Schema
Every event transmitted through Kafka adheres to an enterprise envelope:
\`\`\`json
{
  "eventId": "e9b2512a-89a1-43ef-b72e-8a2bf1893c01",
  "eventType": "PolicyIssuedEvent",
  "eventVersion": "1.0",
  "timestamp": "2026-09-30T10:15:30.125Z",
  "correlationId": "corr-78a9c2b4-5211",
  "aggregateId": "POL-2026-0089",
  "aggregateType": "POLICY",
  "payload": {
    "policyId": 89,
    "policyNumber": "POL-2026-0089",
    "customerId": 15,
    "planId": 3,
    "totalPremium": 14500.00,
    "paymentFrequency": "ANNUAL",
    "startDate": "2026-10-01",
    "endDate": "2027-09-30"
  }
}
\`\`\`

## 8.3 Kafka Topics & Event Matrix

| Kafka Topic | Event Name | Producer Service | Consumer Service(s) | Business Action Triggered |
| :--- | :--- | :--- | :--- | :--- |
| **\`policy-events\`** | \`PolicyIssuedEvent\` | \`policy-service\` | \`premium-service\`, \`notification-service\`, \`reporting-service\` | Premium creates installment schedule; Notification emails welcome kit; Reporting records sales. |
| **\`policy-events\`** | \`PremiumScheduleFailedEvent\` | \`premium-service\` | \`policy-service\` (\`PolicySagaCompensationConsumer\`) | **Saga Rollback**: Reverts policy from \`ISSUED\` to \`DRAFT\` or \`CANCELLED\`. |
| **\`policy-events\`** | \`PolicyActivatedEvent\` | \`policy-service\` | \`notification-service\`, \`reporting-service\` | Customer notified that coverage is active; Analytics updates active in-force book. |
| **\`premium-events\`** | \`PremiumPaidEvent\` | \`payment-service\` | \`policy-service\`, \`premium-service\`, \`notification-service\` | Activates policy if initial; marks installment as \`PAID\`; emails payment receipt. |
| **\`claim-events\`** | \`ClaimSubmittedEvent\` | \`claims-service\` | \`notification-service\`, \`reporting-service\` | Sends acknowledgment SMS with claim tracking number. |
| **\`claim-events\`** | \`ClaimApprovedEvent\` | \`claims-service\` | \`notification-service\`, \`reporting-service\` | Dispatches EOB breakdown to patient and provider; updates claim payout ledger. |
| **\`claim-events\`** | \`ClaimRejectedEvent\` | \`claims-service\` | \`notification-service\`, \`reporting-service\` | Sends formal rejection notice detailing policy exclusions or clause violations. |
| **\`risk-events\`** | \`RiskAssessmentCompletedEvent\` | \`risk-service\` | \`underwriting-service\` | Automatically feeds calculated risk score into underwriting decision table. |
| **\`underwriting-events\`**| \`UnderwritingApprovedEvent\` | \`underwriting-service\` | \`policy-service\` | Notifies policy engine that quote is certified for binding and issuance. |

## 8.4 REST Synchronous vs Kafka Asynchronous Architecture Comparison

| Dimension | REST / OpenFeign Synchronous | Apache Kafka Asynchronous |
| :--- | :--- | :--- |
| **Communication Style** | Request / Response (Point-to-Point) | Publish / Subscribe (Event-Driven) |
| **Coupling** | High temporal coupling (Both caller and receiver must be alive) | Low temporal coupling (Producer finishes immediately) |
| **Latency** | Immediate return of computed data | Eventual consistency (Milliseconds to seconds) |
| **Use Case in HIMS** | Claim eligibility checks (Needs instant YES/NO answer from PolicyService) | Premium schedule creation, payment notifications, BI reporting |
| **Failure Behavior** | Circuit Breaker fallback or immediate HTTP 500 error | Event buffered safely in Kafka topic for automatic consumer retry |

---
`;
  }
};
