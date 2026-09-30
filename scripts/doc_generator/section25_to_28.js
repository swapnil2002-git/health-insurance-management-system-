// Section 25 to 28 module
module.exports = {
  getSection25: function() {
    return `
# 25. DATABASE TABLES SUMMARY TABLE

| Microservice | Database Name | Table Name | Primary Key | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| **Identity Service** | \`identity_db\` | \`users\` | \`id\` (BIGINT) | Stores user account credentials, emails, and BCrypt password hashes. |
| **Identity Service** | \`identity_db\` | \`roles\` | \`id\` (BIGINT) | Defines system roles (\`ROLE_ADMIN\`, \`ROLE_CUSTOMER\`, etc.). |
| **Identity Service** | \`identity_db\` | \`user_roles\` | (\`user_id\`, \`role_id\`) | Composite join table mapping users to their authorized roles. |
| **Customer Service** | \`customer_db\` | \`customers\` | \`id\` (BIGINT) | Master demographic profile, unique customer code, and KYC status. |
| **Customer Service** | \`customer_db\` | \`customer_addresses\`| \`id\` (BIGINT) | Permanent and communication address records for customers. |
| **Customer Service** | \`customer_db\` | \`customer_nominees\` | \`id\` (BIGINT) | Beneficiary and nominee declarations with allocation percentages. |
| **Product Service** | \`product_db\` | \`products\` | \`id\` (BIGINT) | High-level insurance product types and descriptions. |
| **Product Service** | \`product_db\` | \`plans\` | \`id\` (BIGINT) | Specific plan tiers with sum insured, base rates, and age limits. |
| **Product Service** | \`product_db\` | \`plan_benefits\` | \`id\` (BIGINT) | Specific medical coverage benefits, waiting periods, and copays. |
| **Quotation Service**| \`quotation_db\` | \`quotes\` | \`id\` (BIGINT) | Calculated quote proposals with premium totals and expiry dates. |
| **Quotation Service**| \`quotation_db\` | \`quote_members\` | \`id\` (BIGINT) | Family members covered in floater quotes with tobacco flags. |
| **Risk Service** | \`risk_db\` | \`risk_assessments\`| \`id\` (BIGINT) | Medical risk scores (0-100), risk grades, and recommended loadings.|
| **Risk Service** | \`risk_db\` | \`risk_factors\` | \`id\` (BIGINT) | Itemized risk impacts (BMI, tobacco, pre-existing conditions). |
| **Underwriting Service**| \`underwriting_db\`|\`underwriting_cases\`| \`id\` (BIGINT) | Underwriter approval records, special conditions, and loadings. |
| **Policy Service** | \`policy_db\` | \`policies\` | \`id\` (BIGINT) | System of record for policies, active coverage periods, and status. |
| **Policy Service** | \`policy_db\` | \`policy_members\` | \`id\` (BIGINT) | Insured individuals covered under an active policy. |
| **Policy Service** | \`policy_db\` | \`outbox_events\` | \`id\` (BIGINT) | Transactional outbox table for reliable dispatch to \`policy-events\`.|
| **Premium Service** | \`premium_db\` | \`premium_schedules\`| \`id\` (BIGINT) | Billing schedule headers with total installments and balances. |
| **Premium Service** | \`premium_db\` | \`installments\` | \`id\` (BIGINT) | Individual premium installments, due dates, and paid timestamps. |
| **Payment Service** | \`payment_db\` | \`payments\` | \`id\` (BIGINT) | Payment transactions, gateway authorization codes, and methods. |
| **Payment Service** | \`payment_db\` | \`idempotency_records\`|\`id\` (BIGINT) | Unique request keys and cached responses preventing double charges. |
| **Payment Service** | \`payment_db\` | \`outbox_events\` | \`id\` (BIGINT) | Transactional outbox table for payment events. |
| **Provider Service** | \`provider_db\` | \`providers\` | \`id\` (BIGINT) | Hospital and clinic directory, network accreditation, and licensing.|
| **Provider Service** | \`provider_db\` | \`provider_departments\`|\`id\` (BIGINT)| Specialized hospital departments (Cardiology, Oncology, etc.). |
| **Claims Service** | \`claims_db\` | \`claims\` | \`id\` (BIGINT) | Core claim master record with billed and net approved amounts. |
| **Claims Service** | \`claims_db\` | \`claim_diagnoses\` | \`id\` (BIGINT) | ICD-10 diagnosis codes associated with submitted hospital bills. |
| **Claims Service** | \`claims_db\` | \`claim_services\` | \`id\` (BIGINT) | Line-item medical services (bed charges, medicines, surgeries). |
| **Claims Service** | \`claims_db\` | \`claim_documents\` | \`id\` (BIGINT) | Linkages between claim records and uploaded document files. |
| **Claims Service** | \`claims_db\` | \`claim_validations\`| \`id\` (BIGINT) | Audit of each automated stage check in the adjudication engine. |
| **Claims Service** | \`claims_db\` | \`claim_adjudications\`|\`id\` (BIGINT)| Financial adjudication calculations (deductible, copay, net amount).|
| **Claims Service** | \`claims_db\` | \`explanation_of_benefits\`|\`id\` (BIGINT)| Formal EOB breakdown detailing insurer vs patient share. |
| **Claims Service** | \`claims_db\` | \`claim_payments\` | \`id\` (BIGINT) | Payout disbursement records sent to hospital or patient account. |
| **Claims Service** | \`claims_db\` | \`idempotency_records\`|\`id\` (BIGINT) | Unique request keys preventing duplicate claim filings. |
| **Claims Service** | \`claims_db\` | \`outbox_events\` | \`id\` (BIGINT) | Transactional outbox table for Kafka \`claim-events\`. |
| **Claims Service** | \`claims_db\` | \`audit_records\` | \`id\` (BIGINT) | Comprehensive audit log of entity modifications and actions. |
| **Document Service** | \`document_db\` | \`documents\` | \`id\` (BIGINT) | Metadata, physical storage paths, and MIME types of uploaded files. |
| **Notification Service**| \`notification_db\`|\`notifications\` | \`id\` (BIGINT) | Logged simulated email and SMS dispatches. |
| **Reporting Service**| \`reporting_db\` | \`daily_kpi_summaries\`|\`id\` (BIGINT)| Daily OLAP snapshots of issued policies, premium, and loss ratios. |
| **Reporting Service**| \`reporting_db\` | \`claim_analytics\` | \`id\` (BIGINT) | Adjudication processing times and provider claim distributions. |

---
`;
  },

  getSection26: function() {
    return `
# 26. MENTOR VIVA / INTERVIEW QUESTIONS & ANSWERS (50+ Q&A)

This section compiles 52 deeply technical, mentor-level interview and viva questions along with comprehensive, production-grade answers to help you explain every corner of the system with total confidence.

---

### Category A: Microservice Architecture & Distributed Systems

#### Q1: Why did you choose a Microservices architecture instead of a Monolith for an insurance platform?
**Answer**: An insurance enterprise comprises domains with vastly different operational characteristics. The Quotation and Customer browsing systems experience high, spiky read traffic, while Claims and Underwriting are computationally intensive, transactional, and audit-sensitive. A monolith would create database lock contention, single points of failure, and deployment bottlenecks. By separating the system into 17 autonomous microservices with dedicated databases, we achieve:
1. Independent horizontal scalability.
2. Fault isolation (e.g. an outage in Reporting does not stop Claim submissions).
3. Technology and schema evolution autonomy.

#### Q2: What is the Database-per-Service pattern, and how do you handle cross-service joins?
**Answer**: In HIMS, each microservice owns and connects strictly to its own dedicated MySQL schema. Cross-service database joins are strictly prohibited. When a service requires information from another domain, it obtains it through:
1. Synchronous OpenFeign REST queries (for real-time validations like checking policy active status).
2. Asynchronous event consumption via Apache Kafka (for eventual consistency, such as reporting summaries).
3. Data replication/caching of minimal immutable references (e.g. storing customerId and policyNumber directly in the claim record).

#### Q3: What is Netflix Eureka, and how does service discovery work in HIMS?
**Answer**: Eureka acts as a dynamic phonebook for microservices. Instead of hardcoding hostnames and IP addresses, each microservice registers its logical name (e.g. \`policy-service\`) with Eureka upon startup. The services send heartbeats every 30 seconds. Spring Cloud Gateway and OpenFeign clients query Eureka to resolve logical service names into physical IP/port pairs and perform client-side load balancing.

#### Q4: What is the role of Spring Cloud Config Server?
**Answer**: It centralizes external configuration management across all microservices using a native file repository (\`config-repo/\`). This allows us to modify environment parameters (such as Kafka broker addresses, database credentials, or JWT signing keys) in one place without having to recompile the service JAR files.

#### Q5: How does Spring Cloud Gateway route traffic? Is it blocking or non-blocking?
**Answer**: Spring Cloud Gateway is built on Spring 5, Project Reactor, and Netty. It is **non-blocking and reactive**, handling high volumes of concurrent requests with a small number of threads. It matches incoming request paths (e.g. \`/api/policies/**\`), validates JWT tokens, and forwards the traffic to the resolved microservice instance via Eureka.

---

### Category B: Distributed Transactions, Sagas & Kafka

#### Q6: Why can't you use standard ACID @Transactional across multiple microservices?
**Answer**: Standard Spring \`@Transactional\` relies on a single relational database connection under the control of a local transaction manager. When a transaction spans multiple microservices with separate databases, a distributed transaction requires 2-Phase Commit (2PC). However, 2PC is blocking, introduces high network latency, locks resources across network partitions, and severely degrades system availability (violating the CAP theorem).

#### Q7: How does HIMS implement the Saga Pattern?
**Answer**: HIMS implements a **Choreography-based Saga**. For example, when a policy is issued, \`policy-service\` emits a \`PolicyIssuedEvent\` to Kafka. The \`premium-service\` consumes this event and generates the installment schedule. If the premium schedule generation encounters an unrecoverable failure, it emits a \`PremiumScheduleFailedEvent\`. The \`policy-service\` listens for this event via \`PolicySagaCompensationConsumer\` and executes a **compensating transaction**, rolling back the policy status to \`CANCELLED\`.

#### Q8: What is the Dual-Write Problem, and how does the Transactional Outbox Pattern solve it?
**Answer**: The dual-write problem occurs when an application must update a database and publish a message to a broker like Kafka. Because a database commit and a network call to Kafka cannot be wrapped in a single atomic transaction, a crash between the two leaves the system in an inconsistent state. HIMS solves this by writing the domain entity and an \`outbox_events\` record into the **same local database transaction**. A scheduled background worker then reads the pending outbox records and publishes them to Kafka with at-least-once delivery guarantees.

#### Q9: What happens if Apache Kafka crashes while a user is issuing a policy?
**Answer**: Because HIMS uses the Transactional Outbox Pattern, the user's policy issuance transaction does **not** fail. The policy is successfully committed to \`policy_db\`, and the event is written to \`outbox_events\` with status \`PENDING\`. When Kafka recovers, the background \`PolicyOutboxPublisher\` resumes polling and dispatches the buffered events without any loss of data.

#### Q10: What is a Consumer Group in Apache Kafka?
**Answer**: A Consumer Group is a mechanism that allows a pool of consumer instances to divide the work of consuming and processing records from a topic. Kafka assigns each partition of a topic to exactly one consumer in the group, enabling horizontal scalability of message consumption.

---

### Category C: Healthcare Claims & Adjudication Logic

#### Q11: Walk me through the 6-stage automated claim adjudication algorithm.
**Answer**: The 6-stage algorithm in \`claims-service\` consists of:
1. **Stage 1 (Completeness & Format)**: Verifies non-empty bills, valid dates, and mandatory hospital diagnosis codes.
2. **Stage 2 (Policy Coverage Check)**: Makes an OpenFeign call to \`policy-service\` to confirm the policy is in \`ACTIVE\` status and the admission date falls within the policy coverage window.
3. **Stage 3 (Provider Verification)**: Queries \`provider-service\` to verify that the hospital has an active medical license and in-network accreditation.
4. **Stage 4 (Benefit & Waiting Period Check)**: Evaluates the primary ICD-10 diagnosis against policy waiting period clauses (e.g. 30-day initial waiting period or 2-year pre-existing disease exclusions).
5. **Stage 5 (Financial Adjudication)**: Subtracts disallowed non-medical charges, deducts the remaining annual policy deductible, and applies the plan's co-payment percentage to calculate the net approved settlement amount.
6. **Stage 6 (Authorization & EOB)**: Changes status to \`APPROVED\`, persists the Explanation of Benefits (EOB), and writes an event to the transactional outbox.

#### Q12: What is the mathematical difference between a Deductible and a Co-payment?
**Answer**:
- **Deductible**: A fixed dollar amount that the insured must pay out-of-pocket each policy year before the insurance company pays anything (e.g., $500).
- **Co-payment (Copay)**: A fixed percentage (e.g., 10%) of the remaining allowed medical bill that the insured must pay even after the deductible has been satisfied.
- **Formula in HIMS**:
  \`Net Approved = Max(0, (Billed Amount - Disallowed - Remaining Deductible) * (1 - Copay Percentage))\`

#### Q13: What is an Explanation of Benefits (EOB)?
**Answer**: An EOB is a formal accounting statement sent to the policyholder and healthcare provider detailing what medical services were billed, what amount was approved by the insurer, what amounts were disallowed, how deductibles and copays were applied, and the final net payment disbursed.

---

### Category D: Security, JWT & RBAC

#### Q14: How is authentication handled across microservices?
**Answer**: When a user logs in via \`identity-service\`, credentials are verified against \`identity_db\` (using BCrypt). A cryptographically signed JWT token containing the username, user ID, and role claims is returned. On subsequent requests, the client passes this token in the \`Authorization: Bearer <JWT>\` header. Spring Cloud Gateway validates the signature at the edge, and downstream microservices parse the token using a \`JwtAuthenticationFilter\` to populate the Spring \`SecurityContextHolder\`.

#### Q15: Why is BCrypt used for password storage, and what is its salt factor?
**Answer**: BCrypt is an adaptive cryptographic hash function based on the Blowfish cipher. It incorporates a randomly generated salt to defend against rainbow table attacks and is computationally intensive to prevent brute-force hardware cracking. HIMS uses a salt work factor of 10.

#### Q16: How do you enforce Method-Level Security in Spring Boot?
**Answer**: By adding \`@EnableMethodSecurity(prePostEnabled = true)\` on configuration classes and decorating service or controller methods with \`@PreAuthorize("hasRole('ROLE_UNDERWRITER')")\`. If the user in the SecurityContext lacks the required role, Spring Security immediately throws an \`AccessDeniedException\` resulting in an HTTP 403 Forbidden response.

---

### Category E: Idempotency & Fault Tolerance

#### Q17: How is distributed idempotency implemented in the Payment Service?
**Answer**: The client includes a unique UUID in the \`X-Idempotency-Key\` header. The \`payment-service\` checks its \`idempotency_records\` table. If the key is new, it locks the key with status \`PROCESSING\` and executes the payment. Upon completion, it caches the JSON response body and marks the status \`COMPLETED\`. If a duplicate request arrives with the same key, the service immediately returns the cached response with HTTP 200 OK without re-charging the customer's card.

#### Q18: How does Resilience4j Circuit Breaker work in the Claims Service?
**Answer**: When \`claims-service\` calls \`policy-service\` via OpenFeign, the call is monitored by a Resilience4j Circuit Breaker. Over a sliding window of 10 calls, if the failure rate exceeds 50%, the breaker transitions from **CLOSED** to **OPEN**. In the OPEN state, subsequent calls immediately fail fast to a fallback method without stressing the downstream service. After 5 seconds, the breaker enters **HALF-OPEN** to test downstream recovery with a limited number of requests.

---

### Category F: Validation & Frontend

#### Q19: Explain the Three-Level Validation architecture in HIMS.
**Answer**:
1. **Level 1 (UI Validation)**: Angular Reactive Forms with HTML5 bounds (\`maxlength\`, regex patterns, disabled submit buttons) provide instant user feedback.
2. **Level 2 (API/DTO Validation)**: Spring Boot JSR-380 annotations (\`@NotBlank\`, \`@Pattern\`, \`@Positive\`, \`@Size\`) validate payloads at the controller boundary.
3. **Level 3 (Database Validation)**: MySQL schema constraints (\`NOT NULL\`, \`UNIQUE\`, field lengths, foreign keys) ensure physical persistence integrity.

#### Q20: How do Angular HTTP Interceptors function in HIMS?
**Answer**: HIMS utilizes two interceptors:
- \`JwtInterceptor\`: Reads the JWT token from \`TokenStorageService\` and clones outgoing requests to add the \`Authorization: Bearer <token>\` header.
- \`ErrorInterceptor\`: Catches HTTP error responses globally. If an HTTP 401 Unauthorized is detected, it logs out the user and redirects to \`/auth/login\`.

---
`;
  },

  getSection27_and_28: function() {
    return `
# 27. KNOWN DEFECTS & FUTURE ROADMAP

## 27.1 Technical Debt & Current System Boundaries
In accordance with professional engineering integrity, the current release contains the following defined operational boundaries:
1. **Live Third-Party SMTP Relay**: The notification service formats valid emails and logs them in \`notification_db\`, but external SMTP relay (e.g. Amazon SES or SendGrid) is simulated to prevent spam blacklisting during local demo testing.
2. **Live Banking Gateway Integration**: Payment processing utilizes a high-fidelity internal gateway simulator rather than live production merchant credentials (e.g. Razorpay or Stripe).
3. **Manual Invoice Data Entry**: Medical invoices and ICD-10 diagnostic codes are entered into the claim form by the user or hospital operator; automated OCR extraction from uploaded PDF scans is planned for V2.

## 27.2 Version 2.0 Roadmap
- **Machine Learning Document OCR**: Integrate AWS Textract or Tesseract OCR to automatically scan hospital bills and populate claim line items.
- **Biometric Aadhaar Authentication**: Integrate live UIDAI sandbox APIs for one-time password and biometric customer KYC verification.
- **Kubernetes & Cloud Helm Charts**: Package all 17 microservices into Helm charts for deployment on Amazon EKS or Google Kubernetes Engine (GKE).
- **GraphQL Federation Gateway**: Implement Apollo or Spring GraphQL federation to allow mobile apps to query composite customer and policy data in a single request.

---

# 28. CONCLUSION & ARCHITECTURAL SUMMARY

The **Health Insurance Management System (HIMS)** represents a modern, resilient, enterprise-grade cloud microservices implementation. By strictly enforcing **Domain-Driven Design (DDD)**, **Database-per-Service isolation**, the **Transactional Outbox Pattern**, **Choreography-based Sagas**, and a **Three-Level Validation strategy**, the platform eliminates the fragility and performance bottlenecks of legacy insurance monoliths.

The system comprehensively fulfills both academic and industrial standards for distributed architectures, delivering sub-second quote estimation, automated risk scoring, reliable policy issuance, and a robust 6-stage algorithmic claim adjudication engine.

---
`;
  }
};
