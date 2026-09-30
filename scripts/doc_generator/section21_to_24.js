// Section 21 to 24 module
module.exports = {
  getSection21: function() {
    return `
# 21. DEMO WALKTHROUGH SCRIPT

Use this proven, step-by-step 15-minute live demonstration script to present HIMS confidently to your project guide.

---

## 21.1 Live Demo Schedule & Execution Guide

### Minute 00:00 – 02:00: Architecture Verification & Discovery Registry
1. **Open Browser** and navigate to Eureka Service Registry: \`http://localhost:8761\`
2. **Guide Explanation**:
   > *"Respected Guide, as you can see on the Eureka Dashboard, all 15 business and infrastructure microservices have registered successfully. Each service runs on an isolated port with zero cross-database dependencies."*
3. Point out critical instances: \`API-GATEWAY (8080)\`, \`POLICY-SERVICE (8088)\`, \`CLAIMS-SERVICE (8092)\`.

---

### Minute 02:00 – 05:00: Customer Onboarding, Plan Selection & Actuarial Quote
1. **Navigate to UI**: \`http://localhost:4200/auth/login\`
2. Click **"Register New Customer"**:
   - Name: *John Doe*, Email: *john.doe@demo.com*, Phone: *9876543210*, PAN: *ABCDE1234F*.
   - Submit form. Note the instantaneous Level 1 validation and backend generation of \`CUST-2026-0001\`.
3. **Navigate to Plans**: Click on **"Health Companion Silver Plan"** ($500,000 Sum Insured).
4. Click **"Get Instant Quote"**:
   - Add Self (Age 32), Spouse (Age 30), Non-smoker.
   - Click **"Calculate Premium"**: Show the actuarial calculation: Base ($12,000) + Age bracket factor (1.1) + Family Floater loading + 18% GST = **$14,500.00**.
   - Note the generated Quote Number: \`QUO-2026-XXXX\`.

---

### Minute 05:00 – 08:00: Health Risk Scoring & Underwriting Approval
1. Click **"Proceed to Health Disclosures"**:
   - Input Height: 178 cm, Weight: 74 kg (BMI: 23.4 - Normal).
   - Pre-existing diseases: None declared.
   - Click **"Run Automated Risk Assessment"**:
   - Show Risk Score: **18 / 100** (Risk Grade: \`LOW\`).
2. **Underwriting Workflow**:
   - System displays: *"Auto-underwriting qualified. Case certified for standard policy terms."*
   - (Optional: Log in as \`ROLE_UNDERWRITER\` to show the senior underwriter dashboard approving medium/high-risk cases).
3. Click **"Issue Policy"**:
   - The system calls \`policy-service\` \`/api/policies/issue\`.
   - Show created policy: \`POL-2026-0089\` with status \`ISSUED\`.

---

### Minute 08:00 – 11:00: Saga Billing Schedule & Idempotent Premium Payment
1. **Explain the Saga Event Flow to Guide**:
   > *"Upon policy issuance, policy-service wrote a PolicyIssuedEvent to its transactional outbox. The scheduled relayer dispatched this to Kafka 'policy-events'. In the background, premium-service consumed this event and generated an installment schedule."*
2. **View Premium Schedule**:
   - Show the 4 quarterly installments of **$3,625.00** each with specific due dates.
3. **Pay First Installment**:
   - Click **"Pay Installment #1"**.
   - Show the mock payment modal. Click **"Confirm Payment"**.
   - Show the \`X-Idempotency-Key\` in the developer network tab.
   - Note: Installment transitions to \`PAID\`, and Policy status transitions from \`ISSUED\` to **\`ACTIVE\`**.

---

### Minute 11:00 – 14:00: Cashless Claim Submission & 6-Stage Adjudication
1. Navigate to **"Hospital Claim Portal"** (\`/claims/submit\`):
   - Policy Number: \`POL-2026-0089\`
   - Hospital Code: \`HOSP-MAX-001\` (Max Healthcare - In-Network Tier 1)
   - Admission Date: *Current Date - 2 days*, Discharge Date: *Current Date*
   - Diagnosis ICD-10: \`J18.9\` (Pneumonia)
   - Billed Amount: **$45,000.00**
2. Click **"Submit Claim for Adjudication"**:
3. **Open Adjudication Inspector**:
   - Walk your guide through the 6 automated stages displayed on the UI:
     - Stage 1: Format & Bill Completeness -> **PASSED**
     - Stage 2: Policy Coverage Active Verification (OpenFeign) -> **PASSED**
     - Stage 3: Hospital Network & License Accreditation (OpenFeign) -> **PASSED**
     - Stage 4: ICD-10 Waiting Period Clause Evaluation -> **PASSED**
     - Stage 5: Deductible ($500) & Copay (10%) Computation:
       - Billed: $45,000.00
       - Deductible Subtracted: -$500.00
       - 10% Patient Copay: -$4,450.00
       - **Net Approved Settlement: $40,050.00**
     - Stage 6: Final Decision -> **APPROVED**
4. Show generated **Explanation of Benefits (EOB)** document with printable summary.

---

### Minute 14:00 – 15:00: Transactional Outbox Verification & Reporting Dashboard
1. Open MySQL CLI or workbench and run:
   \`\`\`sql
   SELECT id, aggregate_id, event_type, status FROM claims_db.outbox_events;
   \`\`\`
   - Show the record with \`status = 'PUBLISHED'\`, proving that no event is lost during outages.
2. Navigate to **Executive BI Dashboard** (\`/reports/dashboard\`):
   - Show real-time KPI cards: Total Policies Issued, Premium Inflow, Claim Loss Ratio.
3. Conclude demonstration and open the floor for mentor questions.

---
`;
  },

  getSection22: function() {
    return `
# 22. ADVANCED TOPICS & ARCHITECTURAL HIGHLIGHTS

## 22.1 Transactional Outbox Pattern Deep-Dive
One of the most complex challenges in distributed systems is the **Dual-Write Problem**:
- If an application updates its database and immediately attempts to send a message to Kafka, network failure during the Kafka call results in data written to the database without the corresponding event being published.
- If the application publishes to Kafka first and the database write fails, other services react to an event that does not exist in the primary system of record.

HIMS solves this using the **Transactional Outbox Pattern**:
1. Within a single \`@Transactional\` boundary in MySQL, the domain entity and an \`outbox_events\` row are inserted simultaneously using standard InnoDB ACID semantics.
2. An asynchronous scheduled poller (\`PolicyOutboxPublisher\` / \`ClaimOutboxPublisher\`) reads \`PENDING\` events from the outbox table.
3. After Kafka acknowledges receipt (\`SendResult\`), the poller marks the outbox status as \`PUBLISHED\`.
4. If Kafka is unavailable, events remain safely stored in MySQL, providing **At-Least-Once Delivery Guarantees**.

## 22.2 Choreography Saga vs Orchestrator Saga
HIMS utilizes **Choreography-based Sagas** rather than an Orchestrator:
- **No Single Point of Failure**: Eliminates a heavyweight central orchestrator service that could become a bottleneck.
- **Loose Coupling**: Services subscribe to events they care about without requiring an external controller to dictate their execution sequence.
- **Compensating Transactions**: Handled reactively via dedicated Kafka consumers (e.g. \`PolicySagaCompensationConsumer\`).

## 22.3 Polyglot Persistence Readiness
Because each microservice strictly encapsulates its database:
- The \`claims-service\` could be migrated from MySQL to MongoDB (for flexible document storage of heterogeneous hospital bills) with zero modifications to \`policy-service\` or \`customer-service\`.
- The \`reporting-service\` could transition to PostgreSQL or ClickHouse for OLAP aggregation without impacting transactional systems.

---
`;
  },

  getSection23: function() {
    return `
# 23. CODEBASE DIRECTORY MAP

Below is the verified structural directory map of the HIMS repository:

\`\`\`
health-insurance-management-system/
├── pom.xml                                  # Root Maven POM managing dependencies
├── config-repo/                             # Centralized Spring Cloud Config Repository
│   ├── application.yml                      # Global shared configurations
│   ├── api-gateway.yml                      # Gateway routes, CORS, discovery locator
│   ├── identity-service.yml                 # JWT secrets, token expiry, identity_db
│   ├── customer-service.yml                 # customer_db, port 8083
│   ├── product-plan-service.yml             # product_db, port 8084
│   ├── quotation-service.yml                # quotation_db, port 8085
│   ├── risk-service.yml                     # risk_db, port 8086
│   ├── underwriting-service.yml             # underwriting_db, port 8087
│   ├── policy-service.yml                   # policy_db, Kafka policy-events, port 8088
│   ├── premium-service.yml                  # premium_db, installment schedules, port 8089
│   ├── payment-service.yml                  # payment_db, mock gateway, port 8090
│   ├── provider-service.yml                 # provider_db, hospital network, port 8091
│   ├── claims-service.yml                   # claims_db, 6-stage adjudication, Resilience4j, port 8092
│   ├── document-service.yml                 # document_db, storage paths, port 8093
│   ├── notification-service.yml             # notification_db, Kafka consumers, port 8094
│   └── reporting-service.yml                # reporting_db, analytics consumers, port 8095
├── services/                                # 17 Independent Spring Boot Microservices
│   ├── config-server/                       # Spring Cloud Config Server (Port 8888)
│   ├── eureka-server/                       # Netflix Eureka Service Registry (Port 8761)
│   ├── api-gateway/                         # Spring Cloud Netty Gateway (Port 8080)
│   ├── identity-service/                    # Auth, BCrypt, JWT generation (Port 8081)
│   ├── customer-service/                    # Customer master, KYC (Port 8083)
│   ├── product-plan-service/                # Products, plans, benefits (Port 8084)
│   ├── quotation-service/                   # Actuarial pricing engine (Port 8085)
│   ├── risk-service/                        # Health risk scoring algorithm (Port 8086)
│   ├── underwriting-service/                # Case evaluation & loadings (Port 8087)
│   ├── policy-service/                      # Policy lifecycle & Outbox (Port 8088)
│   ├── premium-service/                     # Installment schedules & Kafka listeners (Port 8089)
│   ├── payment-service/                     # Idempotent payments & mock PG (Port 8090)
│   ├── provider-service/                    # Hospital network registry (Port 8091)
│   ├── claims-service/                      # 6-Stage Adjudication & Resilience4j (Port 8092)
│   ├── document-service/                    # File uploads & metadata indexing (Port 8093)
│   ├── notification-service/                # Email/SMS logging consumers (Port 8094)
│   └── reporting-service/                   # OLAP aggregation & KPI summaries (Port 8095)
├── frontend/                                # Angular 17 Single Page Application
│   └── hims-ui/                             # Angular CLI Workspace
│       ├── package.json                     # Angular dependencies
│       └── src/app/
│           ├── core/                        # Auth, Interceptors, Guards, Models
│           ├── features/                    # Feature modules (claim, policy, quote, etc.)
│           └── shared/                      # Reusable UI widgets, cards, modals
├── docker/                                  # Docker & Docker Compose deployment assets
│   └── docker-compose.yml                   # MySQL & Kafka cluster orchestration
└── documentation/                           # Project documentation, PDF exports & guides
\`\`\`

---
`;
  },

  getSection24: function() {
    return `
# 24. API REFERENCE SUMMARY TABLE

| Service | Verb | Endpoint URI | Authorization / Roles | Summary Description |
| :--- | :--- | :--- | :--- | :--- |
| **Identity** | POST | \`/api/auth/register\` | Public | Registers a new user account with BCrypt password hashing. |
| **Identity** | POST | \`/api/auth/login\` | Public | Authenticates credentials; returns signed 24-hr JWT Bearer token. |
| **Customer** | POST | \`/api/customers\` | \`ROLE_CUSTOMER\`, \`ROLE_AGENT\`, \`ROLE_ADMIN\` | Creates master customer profile with addresses and nominees. |
| **Customer** | GET | \`/api/customers/{id}\` | Authenticated | Retrieves detailed customer profile. |
| **Product** | GET | \`/api/products\` | Public | Lists all active insurance products (Individual, Floater, Senior). |
| **Product** | GET | \`/api/plans\` | Public | Lists insurance plans with sum insured, deductible, and copay terms. |
| **Quotation**| POST | \`/api/quotes\` | Authenticated | Calculates actuarial quote based on age, tobacco, and sum insured. |
| **Quotation**| GET | \`/api/quotes/{id}\` | Authenticated | Retrieves quote summary and validity status. |
| **Risk** | POST | \`/api/risk-assessments\` | Authenticated | Calculates composite risk score (0-100) and assigns risk grade. |
| **Underwriting**| POST | \`/api/underwriting/evaluate\`| Authenticated | Evaluates quote; auto-approves low risk or queues for review. |
| **Underwriting**| PUT | \`/api/underwriting/{id}/approve\`| \`ROLE_UNDERWRITER\`, \`ROLE_ADMIN\` | Underwriter manually approves case with premium loading adjustments. |
| **Policy** | POST | \`/api/policies/issue\` | \`ROLE_UNDERWRITER\`, \`ROLE_AGENT\`, \`ROLE_ADMIN\` | Issues policy, records transactional outbox event. |
| **Policy** | GET | \`/api/policies/number/{num}\`| Authenticated | Queries policy active dates and coverage (consumed by Claims). |
| **Policy** | PUT | \`/api/policies/{id}/activate\`| Internal / Admin | Activates policy upon initial premium settlement. |
| **Premium** | POST | \`/api/premium-schedules/generate\`| Authenticated | Generates installment breakdown with due dates and grace periods. |
| **Payment** | POST | \`/api/payments/process\` | Authenticated (\`X-Idempotency-Key\`) | Processes mock card/UPI payment with idempotency guarantee. |
| **Provider** | GET | \`/api/providers\` | Public / Authenticated | Lists accredited healthcare providers with network status filters. |
| **Claims** | POST | \`/api/claims\` | Authenticated (\`X-Idempotency-Key\`) | Submits claim, persists dossier, initiates automated validation. |
| **Claims** | POST | \`/api/claims/{id}/adjudicate\`| \`ROLE_CLAIMS_OFFICER\`, \`ROLE_ADMIN\` | Runs 6-stage adjudication engine; calculates deductible & copay. |
| **Claims** | GET | \`/api/claims/{id}/eob\` | Authenticated | Retrieves Explanation of Benefits breakdown for patient/hospital. |
| **Document** | POST | \`/api/documents/upload\` | Authenticated | Uploads medical bills, KYC documents, or policy documents. |
| **Reporting**| GET | \`/api/reports/dashboard-kpis\`| \`ROLE_ADMIN\` | Serves aggregated business intelligence and loss ratio metrics. |

---
`;
  }
};
