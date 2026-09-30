// Section 17 to 20 module
module.exports = {
  getSection17: function() {
    return `
# 17. DESIGN PATTERNS USED IN HIMS

HIMS incorporates proven enterprise software design patterns across its architectural, creational, structural, and behavioral tiers.

## 17.1 Architectural & Distributed Patterns

| Pattern | Where Used | Implementation Details & Purpose |
| :--- | :--- | :--- |
| **Transactional Outbox** | \`claims-service\`, \`policy-service\`, \`payment-service\` | Solves the dual-write problem by saving domain events in a relational \`outbox_events\` table in the same ACID transaction as the business entity. A background scheduled poller relays events to Kafka. |
| **Choreography-based Saga** | Multi-service (\`policy\`, \`premium\`, \`payment\`) | Coordinates distributed transactions without a centralized orchestrator. Services emit domain events; downstream services react or trigger compensating transactions (\`PolicySagaCompensationConsumer\`). |
| **Circuit Breaker & Retry** | \`claims-service\` via Resilience4j | Protects synchronous OpenFeign calls to \`policy-service\` and \`provider-service\` from cascading latency or service outages with configurable failure rate thresholds and exponential backoffs. |
| **Database-per-Service** | All 13 business microservices | Enforces domain boundary autonomy. Zero shared databases or cross-service SQL joins; data exchange occurs exclusively via REST APIs or Kafka events. |
| **API Gateway / Reverse Proxy**| \`api-gateway\` (Spring Cloud Gateway) | Centralizes authentication, SSL termination, CORS pre-flight, and dynamic route resolution via Eureka. |

## 17.2 Creational & Structural Patterns

| Pattern | Where Used | Implementation Details & Purpose |
| :--- | :--- | :--- |
| **Builder Pattern** | All DTOs, Entities, and Event Envelopes | Utilizes Lombok's \`@Builder\` to cleanly instantiate complex immutable objects (e.g. \`ClaimAdjudication\`, \`EventEnvelope\`) with readable, parameter-safe syntax. |
| **Factory Pattern** | \`notification-service\`, \`payment-service\` | \`NotificationFactory\` instantiates specific email or SMS channel dispatchers based on recipient preferences. \`PaymentProcessorFactory\` instantiates UPI, Card, or NetBanking simulation handlers. |
| **Adapter Pattern** | \`payment-service\`, \`document-service\` | Adapts raw third-party payment gateway callbacks and local file storage paths into standardized internal domain DTOs. |
| **Singleton Pattern** | All Spring Beans | Enforced by the Spring IoC container for all \`@Service\`, \`@Repository\`, and \`@Component\` beans, ensuring thread-safe, single-instance lifecycle management. |

## 17.3 Behavioral Patterns

| Pattern | Where Used | Implementation Details & Purpose |
| :--- | :--- | :--- |
| **Strategy Pattern** | \`quotation-service\`, \`claims-service\` | \`PremiumCalculationStrategy\` selects different actuarial algorithms based on plan type (Individual vs Family Floater). \`AdjudicationRuleStrategy\` applies modular validation rules across claim stages. |
| **Interceptor / Filter Pattern**| \`api-gateway\`, \`hims-ui\` | \`JwtAuthenticationFilter\` intercepts HTTP requests to populate the SecurityContext; Angular \`JwtInterceptor\` injects bearer tokens on all client calls. |
| **Repository Pattern** | Spring Data JPA Repositories | Abstract data access behind \`JpaRepository\` interfaces, decoupling domain business logic from specific SQL queries and Hibernate ORM mechanics. |

---
`;
  },

  getSection18: function() {
    return `
# 18. FAILURE SCENARIOS & RECOVERY STRATEGIES

To ensure 99.99% operational availability and prevent financial loss, HIMS implements automated mitigation for critical failure modes:

## 18.1 Failure Scenarios Matrix

| # | Failure Scenario | Trigger / Root Cause | Immediate System Impact | Automated Recovery / Mitigation Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **Payment Processing Failure** | Insufficient funds, gateway timeout, or bank decline during premium checkout. | Transaction marked \`FAILED\`. Policy remains in \`ISSUED\` status (not active). | System returns clear error code to user. Premium schedule maintains pending balance. User can retry with an alternate card or UPI method. No duplicate policies created. |
| **2** | **Apache Kafka Broker Outage** | Network partition, broker crash, or disk saturation on port 9092. | Real-time event consumption pauses; notifications and analytics delayed. | **Outbox Buffer Protection**: Microservices do not fail user requests. Events remain safely buffered in MySQL \`outbox_events\` with status \`PENDING\`. Once Kafka recovers, scheduled relayers resume dispatch automatically. |
| **3** | **Downstream Policy Service Unavailable** | \`policy-service\` pod crashes during claim submission eligibility check. | OpenFeign call from \`claims-service\` encounters socket timeout. | **Resilience4j Circuit Breaker**: Activates fallback method after 50% failure rate. Claim is transitioned to \`IN_REVIEW\` (Queued for offline validation) rather than throwing an HTTP 500 error to the hospital. |
| **4** | **Duplicate Claim Submission Attempt** | Hospital automation software submits the same hospital admission twice within seconds. | Potential risk of double claim payout. | **Idempotency Barrier**: The \`X-Idempotency-Key\` or composite natural key (patientId + providerCode + admissionDate) intercepts the second request, returning the existing claim reference immediately with zero duplicate database rows. |
| **5** | **Expired Quotation Conversion** | Customer attempts to issue a policy using a quote generated 45 days ago (validity: 30 days). | Pricing risk due to stale age/actuarial brackets. | \`PolicyService\` validates \`quote.getExpiryDate().isBefore(LocalDate.now())\`. Rejects transaction with HTTP 400 (\`QUOTE_EXPIRED\`) and prompts the user to recalculate a fresh quote. |
| **6** | **Database Connection Pool Saturation** | Surge in concurrent claim submissions during regional epidemic. | HikariCP pool exhausted, queries queueing. | Fast fail configuration with HikariCP \`connection-timeout: 3000ms\`. Non-critical analytical queries routed to read-only replica databases or deferred to Kafka consumers. |

---
`;
  },

  getSection19: function() {
    return `
# 19. IMPLEMENTED VS UNIMPLEMENTED FEATURES

To ensure complete academic and technical transparency during your mentor demonstration, this section explicitly delineates features that are **fully functional in source code** versus those that are **architecturally documented but simplified or planned for V2**.

## 19.1 Feature Implementation Matrix

| Domain / Component | Feature Description | Status in Current Codebase | Verification & Implementation Notes |
| :--- | :--- | :--- | :--- |
| **Core Architecture** | 17 Microservices Structure | ✅ IMPLEMENTED | All 17 services configured in root Maven POM and \`config-repo/\`. |
| **Service Discovery** | Netflix Eureka Registry | ✅ IMPLEMENTED | Running on port 8761 with automatic heartbeat registration. |
| **Configuration** | Centralized Spring Cloud Config | ✅ IMPLEMENTED | Running on port 8888 reading from \`config-repo/*.yml\`. |
| **API Routing** | Spring Cloud Gateway with CORS | ✅ IMPLEMENTED | Running on port 8080 with dynamic path discovery and JWT filters. |
| **Authentication** | JWT Generation & BCrypt Hashing | ✅ IMPLEMENTED | Implemented in \`identity-service\` with 24-hr expiry and HMAC-SHA256. |
| **Authorization** | Role-Based Access Control (RBAC)| ✅ IMPLEMENTED | \`ROLE_ADMIN\`, \`ROLE_CUSTOMER\`, \`ROLE_UNDERWRITER\`, \`ROLE_CLAIMS_OFFICER\` enforced via \`@PreAuthorize\`. |
| **Customer Master** | KYC & Profile Management | ✅ IMPLEMENTED | Customer onboarding with unique code generation, address & nominee. |
| **Product Catalog** | Products, Plans, and Benefits | ✅ IMPLEMENTED | Full CRUD for individual, floater, and senior citizen plans with benefit limits. |
| **Quotation Engine**| Actuarial Premium Calculation | ✅ IMPLEMENTED | Dynamic quote generation considering age, tobacco, and sum insured. |
| **Risk Scoring** | Automated Health Risk Engine | ✅ IMPLEMENTED | BMI calculation, medical history scoring, and risk grade assignment. |
| **Underwriting** | Case Evaluation & Approval | ✅ IMPLEMENTED | Auto-approval for low risk, underwriter queue for medium/high risk. |
| **Policy Lifecycle**| Draft, Issued, Active, Cancelled | ✅ IMPLEMENTED | Policy generation, versioning, and state management. |
| **Premium Billing** | Installment Schedules | ✅ IMPLEMENTED | Annual, quarterly, and monthly installment breakdown with due dates. |
| **Payment System** | Mock Gateway & Idempotency | ✅ IMPLEMENTED | Simulated card/UPI payment with \`X-Idempotency-Key\` deduplication. |
| **Provider Directory**| In-Network Hospital Governance| ✅ IMPLEMENTED | Hospital registry with network accreditation and licensing status. |
| **Claims Engine** | 6-Stage Automated Adjudication | ✅ IMPLEMENTED | Full SDD implementation: format, policy check, provider check, benefit rules, deductible/copay math, and EOB. |
| **Transactional Outbox**| Outbox Table & Scheduled Relayer| ✅ IMPLEMENTED | Implemented in \`claims-service\`, \`payment-service\`, \`policy-service\`. |
| **Choreography Saga**| Event Compensation on Failure | ✅ IMPLEMENTED | \`PolicySagaCompensationConsumer\` rolls back policy state on \`PremiumScheduleFailedEvent\`. |
| **Resilience4j** | Circuit Breaker & Retry | ✅ IMPLEMENTED | Configured on Feign clients in \`claims-service\` with fallback methods. |
| **Document Storage**| File Upload & Path Indexing | ✅ IMPLEMENTED | Multipart file upload to local directory with metadata tracking. |
| **Notifications** | Event-Driven Notification Log | ✅ IMPLEMENTED | Consumes Kafka events and logs simulated email/SMS dispatches. |
| **Real SMTP Email** | Live External Mail Server Relay| ⚠️ PARTIALLY IMPLEMENTED | Notification service formats emails and logs delivery to DB; real SMTP server relay disabled by default to avoid spam errors in local demo. |
| **Real Payment PG** | Live Razorpay / Stripe Gateway | ⚠️ PARTIALLY IMPLEMENTED | Uses high-fidelity internal gateway simulator rather than production merchant API keys. |
| **OCR Document AI** | Machine Learning Receipt OCR | ❌ NOT IMPLEMENTED (V2)| Claims officer verifies uploaded invoice scans manually; automated optical character recognition planned for V2. |
| **Biometric KYC** | Government UIDAI Aadhaar OTP | ❌ NOT IMPLEMENTED (V2)| Aadhaar number validated via regex format; live government biometric OTP API not integrated due to regulatory sandbox restrictions. |

---
`;
  },

  getSection20: function() {
    return `
# 20. PROJECT SETUP & RUN GUIDE

Follow this exact sequence to start the Health Insurance Management System on your local development workstation.

## 20.1 Environment Prerequisites
- **Java**: JDK 17 (or 21 LTS) installed and configured on \`JAVA_HOME\`.
- **Node.js**: v18+ (or v14+) and npm installed.
- **MySQL Server**: MySQL 8.0 running locally on port 3306 (root password: \`root\` or configured in YAML).
- **Apache Kafka & Zookeeper**: Kafka broker running on \`localhost:9092\` (or via Docker Compose).
- **Maven**: 3.9+ installed and on system \`PATH\`.

---

## 20.2 Database Provisioning Script
Execute the following SQL in your MySQL client to instantiate all required microservice database schemas:
\`\`\`sql
CREATE DATABASE IF NOT EXISTS identity_db;
CREATE DATABASE IF NOT EXISTS customer_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS quotation_db;
CREATE DATABASE IF NOT EXISTS risk_db;
CREATE DATABASE IF NOT EXISTS underwriting_db;
CREATE DATABASE IF NOT EXISTS policy_db;
CREATE DATABASE IF NOT EXISTS premium_db;
CREATE DATABASE IF NOT EXISTS payment_db;
CREATE DATABASE IF NOT EXISTS provider_db;
CREATE DATABASE IF NOT EXISTS claims_db;
CREATE DATABASE IF NOT EXISTS document_db;
CREATE DATABASE IF NOT EXISTS notification_db;
CREATE DATABASE IF NOT EXISTS reporting_db;
\`\`\`

---

## 20.3 Starting Infrastructure via Docker (Optional)
If running MySQL and Kafka through Docker, start the provided docker compose file:
\`\`\`bash
cd docker
docker-compose up -d
\`\`\`

---

## 20.4 Microservices Startup Sequence
> [!IMPORTANT]
> Because microservices depend on centralized configuration and dynamic service discovery, services **MUST** be started in the following strict chronological order:

### Phase 1: Core Cloud Infrastructure (Start First)
1. **Config Server** (Port 8888)
   \`\`\`bash
   cd services/config-server
   mvn spring-boot:run
   \`\`\`
   *Wait until logs indicate: "Started ConfigServerApplication on port 8888"*

2. **Eureka Service Registry** (Port 8761)
   \`\`\`bash
   cd services/eureka-server
   mvn spring-boot:run
   \`\`\`
   *Verify dashboard is accessible at: http://localhost:8761*

3. **API Gateway** (Port 8080)
   \`\`\`bash
   cd services/api-gateway
   mvn spring-boot:run
   \`\`\`

### Phase 2: Domain Microservices
Open separate terminal windows and run:
\`\`\`bash
# Identity & Customer
mvn spring-boot:run -f services/identity-service/pom.xml    # Port 8081
mvn spring-boot:run -f services/customer-service/pom.xml    # Port 8083

# Product, Quotation, Risk & Underwriting
mvn spring-boot:run -f services/product-plan-service/pom.xml # Port 8084
mvn spring-boot:run -f services/quotation-service/pom.xml   # Port 8085
mvn spring-boot:run -f services/risk-service/pom.xml        # Port 8086
mvn spring-boot:run -f services/underwriting-service/pom.xml # Port 8087

# Policy, Premium, Payment & Provider
mvn spring-boot:run -f services/policy-service/pom.xml      # Port 8088
mvn spring-boot:run -f services/premium-service/pom.xml     # Port 8089
mvn spring-boot:run -f services/payment-service/pom.xml     # Port 8090
mvn spring-boot:run -f services/provider-service/pom.xml    # Port 8091

# Claims, Documents, Notifications & Reporting
mvn spring-boot:run -f services/claims-service/pom.xml      # Port 8092
mvn spring-boot:run -f services/document-service/pom.xml    # Port 8093
mvn spring-boot:run -f services/notification-service/pom.xml# Port 8094
mvn spring-boot:run -f services/reporting-service/pom.xml   # Port 8095
\`\`\`

### Phase 3: Angular Frontend Application
\`\`\`bash
cd frontend/hims-ui
npm install
npm start
\`\`\`
*Access the application in your browser at: \`http://localhost:4200\`*

---
`;
  }
};
