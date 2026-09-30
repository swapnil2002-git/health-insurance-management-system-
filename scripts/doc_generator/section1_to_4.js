// Section 1 to 4 module
module.exports = {
  getSection1: function() {
    return `
# 1. PROJECT OVERVIEW

## 1.1 Executive Summary
The **Health Insurance Management System (HIMS)** is an enterprise-grade, distributed, cloud-native healthcare insurance lifecycle management platform. Designed in accordance with modern Domain-Driven Design (DDD) principles and reactive microservices architecture, HIMS automates and orchestrates the end-to-end lifecycle of individual and family health insurance products—spanning digital customer onboarding, dynamic plan catalog discovery, actuarial quote calculation, automated rule-based risk profiling, underwriting approval, policy issuance, flexible premium billing schedules, multi-channel payment reconciliation, healthcare provider network governance, and a fully automated six-stage healthcare claim adjudication engine.

## 1.2 The Real-World Problem Solved
Traditional insurance enterprises and legacy Third-Party Administrators (TPAs) suffer from severe operational friction:
1. **Siloed Monoliths**: Legacy insurance platforms bundle quote estimation, policy management, and claim settlement into fragile monolithic databases, leading to catastrophic outages, database lock contention, and high deployment risks.
2. **Paper-Intensive, Slow Underwriting**: Manual risk assessments take between 5 to 14 business days, driving prospective policyholders to competing digital-first insurtechs.
3. **Fraud and Claim Adjudication Bottlenecks**: Over 30% of claim processing delays stem from manual verification of provider network status, policy eligibility dates, waiting period clauses, and copayment/deductible limits.
4. **Lack of Auditability & Compliance**: HIPAA, GDPR, and insurance regulatory bodies mandate strict traceability of who accessed policy data, why a claim was rejected, and how premiums were calculated.
5. **Billing & Reconciliation Failures**: Disjointed payment processors result in policy cancellations due to false negative premium reconciliations.

HIMS solves these problems through:
- **Zero-Wait Digital Lifecycle**: Sub-second quote calculation and instantaneous underwriting for standard risk profiles.
- **Microservices Isolation**: 17 decoupled services communicating synchronously via Spring Cloud OpenFeign and asynchronously via Apache Kafka event streams.
- **Database-Per-Service Autonomy**: Zero cross-service database foreign keys or joins, ensuring complete operational autonomy and horizontal scalability.
- **Autonomous Claim Adjudication**: Algorithmic validation against real-time policy coverage, provider license validity, ICD-10 diagnostic codes, policy benefit limits, and deductible calculation.
- **Enterprise Resilience & Distributed Consistency**: Transactional Outbox Pattern, Choreography-based Sagas for compensation, distributed Idempotency tables, and Resilience4j circuit breakers.

## 1.3 Target Stakeholders & User Personas
The system provides tailored interfaces, workflows, and access controls for six distinct user personas:
1. **Policyholder / Customer**: Discovers health insurance plans, generates personalized quotes, completes digital KYC, purchases policies, pays recurring premiums, locates in-network hospitals, submits cashless/reimbursement claims, and tracks settlement statuses in real-time.
2. **Insurance Agent / Broker**: Manages customer portfolios, issues quotes on behalf of clients, assists with document uploads, and tracks commission-qualifying issued policies.
3. **Underwriter**: Reviews complex or high-risk applications escalated by the automated risk scoring engine, reviews medical disclosures/pre-existing conditions, applies premium loadings or exclusions, and approves/rejects policies.
4. **Claims Officer / Adjudicator**: Evaluates submitted claims, audits hospital invoices, reviews diagnostic codes, overrides automated adjudication decisions with documented justification, and authorizes payment disbursement.
5. **Healthcare Provider (Hospital/Clinic)**: Checks patient policy active status and pre-authorization eligibility, submits cashless claims directly upon patient admission/discharge, and monitors settlement reconciliation.
6. **Finance & System Administrator**: Oversees premium collection reconciliation, monitors batch payment relays, manages microservice health, inspects audit logs, and analyzes executive KPIs via reporting dashboards.

## 1.4 High-Level Business Domain Architecture
HIMS is segmented into four primary business domains:
\`\`\`
+----------------------------------------------------------------------------------------------------+
|                                    HIMS ENTERPRISE ARCHITECTURE                                    |
+----------------------------------------------------------------------------------------------------+
|  1. POLICY ACQUISITION DOMAIN   |  2. POLICY LIFECYCLE DOMAIN   |  3. CLAIMS & NETWORK DOMAIN      |
|  - Customer Service             |  - Policy Service             |  - Provider Service              |
|  - Product & Plan Service       |  - Premium Service            |  - Claims Adjudication Service   |
|  - Quotation Service            |  - Payment Service            |  - Document Management Service   |
|  - Risk Assessment Service      |                               |                                  |
|  - Underwriting Service         |                               |                                  |
+---------------------------------+-------------------------------+----------------------------------+
|  4. CROSS-CUTTING INFRASTRUCTURE & REPORTING DOMAIN                                                |
|  - Spring Cloud Gateway (8080)  - Eureka Discovery (8761)       - Config Server (8888)             |
|  - Identity & RBAC (8081)       - Notification Service (8094)   - Reporting Service (8095)         |
|  - Apache Kafka Cluster (9092)  - Transactional Outbox Engine   - Resilience4j Circuit Breakers    |
+----------------------------------------------------------------------------------------------------+
\`\`\`

## 1.5 End-to-End Business Flow Diagram
The complete lifecycle flow from initial customer discovery to claim payout:
\`\`\`
   [ Customer Registration & KYC ]
                  │
                  ▼
      [ Product / Plan Selection ]
                  │
                  ▼
         [ Quotation Creation ]
                  │
                  ▼
        [ Risk Assessment Engine ]
                  │
                  ▼
        [ Underwriting Decision ] ───(If High Risk)───► [ Manual Review / Loading ]
                  │                                                │
                  ▼ (Approved)                                    ▼ (Approved)
           [ Policy Issuance ] ◄───────────────────────────────────┘
                  │
                  ▼
      [ Premium Schedule Creation ] (Choreography Saga)
                  │
                  ▼
        [ Payment Transaction ]
                  │
                  ▼
        [ Policy Active State ]
                  │
                  ▼
   [ Hospital Admission & Treatment ] (In-Network / Out-of-Network Provider)
                  │
                  ▼
         [ Claim Submission ]
                  │
                  ▼
   [ 6-Stage Automated Adjudication ]
     ├─ Stage 1: Basic & Format Validation
     ├─ Stage 2: Policy Coverage & Active Status (OpenFeign)
     ├─ Stage 3: Provider Network & License Verification (OpenFeign)
     ├─ Stage 4: Benefit Limit & Waiting Period Check
     ├─ Stage 5: Deductible, Co-pay & Allowed Amount Calculation
     └─ Stage 6: Final Adjudication (Approved / Denied / Manual Review)
                  │
                  ▼
      [ Payment Settlement & EOB ] ──► [ Notification & Reporting Relays ]
\`\`\`
`;
  },

  getSection2: function() {
    return `
# 2. TECHNOLOGY STACK

The Health Insurance Management System is constructed upon a hardened, enterprise-grade technology stack chosen specifically for mission-critical transactional integrity, low latency, fault isolation, and horizontal scalability.

## 2.1 Technology Stack Matrix

| Technology | Layer / Category | Version | Where Used in HIMS | Architectural Justification |
| :--- | :--- | :--- | :--- | :--- |
| **Java** | Programming Language | **17 (LTS)** | All 17 backend microservices | Provides modern LTS features: records, pattern matching, sealed classes, strong memory safety, and high-performance garbage collection (G1GC). |
| **Spring Boot** | Application Framework | **3.2.x** | Core framework for all services | Rapid bootstrapping, autoconfiguration, embedded high-throughput Tomcat 10, Spring AOP, and built-in production metrics. |
| **Spring Cloud** | Cloud Distributed Systems | **2023.0.x** | Distributed infrastructure | Provides centralized discovery (Eureka), declarative HTTP clients (OpenFeign), and centralized Git configuration. |
| **Spring Cloud Gateway**| API Gateway & Routing | **4.1.x** | \`api-gateway\` (Port 8080) | Non-blocking reactive Netty gateway. Handles routing, JWT authentication filtering, global CORS, and load balancing across instances. |
| **Spring Cloud Config** | Configuration Management | **4.1.x** | \`config-server\` (Port 8888) | Native profile-based centralized configuration repository (\`config-repo/*.yml\`). Enables environment-specific parameter changes without recompilation. |
| **Netflix Eureka** | Service Registry & Discovery| **4.1.x** | \`eureka-server\` (Port 8761) | Dynamic microservice discovery. Allows services to find each other by logical service names (e.g. \`http://policy-service\`) rather than hardcoded IPs. |
| **Spring Data JPA** | Object-Relational Mapping | **3.2.x** | All persistent microservices | Rapid repository development, declarative transactions (\`@Transactional\`), derived queries, and standardized entity life-cycle management. |
| **Hibernate Core** | JPA Provider | **6.4.x** | All persistent microservices | Optimized SQL generation, entity state transitions, optimistic locking (\`@Version\`), and schema validation. |
| **MySQL Database** | Relational Database Engine | **8.0.x** | 13 dedicated service databases | ACID-compliant relational persistence, InnoDB storage engine, foreign key enforcement within service boundaries, and indexing. |
| **Apache Kafka** | Distributed Streaming Platform| **3.6.x** | Event broker across all services | High-throughput distributed event log for asynchronous pub-sub, saga choreography, and eventual consistency decoupling. |
| **Spring Kafka** | Kafka Messaging Integration | **3.1.x** | Producers and consumers | Declarative \`@KafkaListener\`, \`KafkaTemplate\`, automatic JSON serialization/deserialization, and consumer offset management. |
| **OpenFeign** | Declarative REST Client | **4.1.x** | Inter-service synchronous RPC | Simplifies HTTP communication between services with interface annotations, automatic load balancing via Spring Cloud LoadBalancer. |
| **Resilience4j** | Fault Tolerance Framework | **2.1.x** | \`claims-service\`, \`policy-service\` | Circuit Breaker, Retry, and Fallback patterns to prevent cascading failures when downstream services experience latency or downtime. |
| **Spring Security** | Security & Access Control | **6.2.x** | All microservices | Filter chain enforcement, stateless session management, RBAC enforcement with \`@PreAuthorize\`, and CORS/CSRF configuration. |
| **JJWT (Java JWT)** | JWT Token Library | **0.11.5** | \`identity-service\`, \`api-gateway\` | Cryptographic creation and verification of HMAC-SHA256 signed bearer tokens carrying user identity, roles, and expiration claims. |
| **BCrypt** | Password Hashing | **Spring Crypto**| \`identity-service\` | One-way salted hashing (\`BCryptPasswordEncoder\`) ensuring zero clear-text password storage in \`identity_db\`. |
| **Hibernate Validator** | Bean Validation (JSR-380) | **8.0.x** | DTOs across all microservices | Declarative data integrity validation using annotations (\`@NotBlank\`, \`@Size\`, \`@Pattern\`, \`@Positive\`, \`@Email\`). |
| **Angular** | Single Page Application (SPA)| **17.x** | \`frontend/hims-ui\` | Component-based, modular client architecture, reactive forms, dependency injection, and RxJS observable pipelines. |
| **TypeScript** | Frontend Language | **5.x** | \`frontend/hims-ui\` | Strict compile-time typing, interfaces matching backend DTOs, and clean object-oriented client structure. |
| **Bootstrap** | CSS UI Framework | **5.3.x** | \`frontend/hims-ui\` | Responsive grid system, accessible navigation, modal dialogs, clean modern enterprise styling, and typography. |
| **FontAwesome** | UI Iconography | **6.5.x** | \`frontend/hims-ui\` | Consistent iconography across dashboards, navigation menus, claim status badges, and action buttons. |
| **Maven** | Build & Dependency Tool | **3.9.x** | Root and all microservices | Standardized multi-module build lifecycle, dependency version alignment, and build automation. |
| **Docker & Docker Compose**| Containerization | **Compose v2** | \`docker/docker-compose.yml\` | Containerized orchestration of MySQL, Apache Kafka, and Zookeeper for reproducible local and CI/CD environments. |

---
`;
  },

  getSection3: function() {
    return `
# 3. SYSTEM ARCHITECTURE

## 3.1 Distributed Microservices Topology
The HIMS architecture is structured into a four-tier distributed topology:
1. **Client Tier**: Angular Single Page Application running in modern web browsers.
2. **Gateway & Security Tier**: Spring Cloud Gateway enforcing routing, CORS pre-flight, and JWT token validation.
3. **Domain Microservices Tier**: 14 autonomous business services executing domain logic, managing isolated databases, and publishing outbox events.
4. **Data & Streaming Tier**: 13 isolated MySQL databases and an Apache Kafka distributed commit log cluster.

\`\`\`
                                  ┌────────────────────────┐
                                  │   Angular 17 Client    │
                                  │   (Port 4200 / UI)     │
                                  └───────────┬────────────┘
                                              │ HTTP / JSON
                                              ▼
                                  ┌────────────────────────┐
                                  │  Spring Cloud Gateway  │
                                  │      (Port 8080)       │
                                  └───────────┬────────────┘
                                              │
              ┌───────────────────────────────┴───────────────────────────────┐
              │ Route Resolution via Eureka Discovery Server (Port 8761)      │
              ▼                                                               ▼
+───────────────────────────+                                   +───────────────────────────+
|      Identity Service     |                                   |     Customer Service      |
|        (Port 8081)        |                                   |        (Port 8083)        |
|    DB: identity_db (MySQL)|                                   |    DB: customer_db (MySQL)|
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|   Product & Plan Service  |                                   |     Quotation Service     |
|        (Port 8084)        |                                   |        (Port 8085)        |
|    DB: product_db (MySQL) |                                   |    DB: quotation_db(MySQL)|
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|      Risk Service         |                                   |   Underwriting Service    |
|        (Port 8086)        |                                   |        (Port 8087)        |
|     DB: risk_db (MySQL)   |                                   |   DB: underwriting_db     |
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|      Policy Service       |                                   |      Premium Service      |
|        (Port 8088)        |                                   |        (Port 8089)        |
|    DB: policy_db (MySQL)  |                                   |    DB: premium_db (MySQL) |
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|      Payment Service      |                                   |     Provider Service      |
|        (Port 8090)        |                                   |        (Port 8091)        |
|    DB: payment_db (MySQL) |                                   |    DB: provider_db (MySQL)|
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|      Claims Service       |                                   |     Document Service      |
|        (Port 8092)        |                                   |        (Port 8093)        |
|    DB: claims_db (MySQL)  |                                   |    DB: document_db (MySQL)|
+───────────────────────────+                                   +───────────────────────────+
              │                                                               │
+───────────────────────────+                                   +───────────────────────────+
|   Notification Service    |                                   |     Reporting Service     |
|        (Port 8094)        |                                   |        (Port 8095)        |
| DB: notification_db(MySQL)|                                   |    DB: reporting_db(MySQL)|
+───────────────────────────+                                   +───────────────────────────+
                                              ▲
                                              │ Asynchronous Event Bus
                                              ▼
                    ═════════════════════════════════════════════════════
                                   APACHE KAFKA CLUSTER (Port 9092)
                       Topics: policy-events, claim-events,
                               premium-events, risk-events
                    ═════════════════════════════════════════════════════
\`\`\`

## 3.2 Service Catalog & Responsibilities

| # | Service Name | Port | Database Name | Primary Responsibility | Sync Clients (OpenFeign) | Kafka Topics Consumed | Kafka Topics Produced |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | \`config-server\` | 8888 | None (Git / Native) | Serves centralized configuration YAML files | None | None | None |
| **2** | \`eureka-server\` | 8761 | In-Memory Registry | Service registration, heartbeat monitoring, and client discovery | None | None | None |
| **3** | \`api-gateway\` | 8080 | None | Single entry point, JWT validation, reverse proxy routing, CORS | Eureka | None | None |
| **4** | \`identity-service\` | 8081 | \`identity_db\` | User registration, authentication, JWT token issuance, RBAC | None | None | None |
| **5** | \`customer-service\` | 8083 | \`customer_db\` | Customer master profile, KYC verification, address & nominee data | None | None | \`customer-events\` |
| **6** | \`product-plan-service\`| 8084 | \`product_db\` | Insurance products, plans, tiers, coverage benefits, deductibles | None | None | None |
| **7** | \`quotation-service\` | 8085 | \`quotation_db\` | Premium estimation based on age, coverage, tobacco use, and members | Customer, Product | None | \`quote-events\` |
| **8** | \`risk-service\` | 8086 | \`risk_db\` | Health risk scoring based on BMI, medical history, age, habits | Customer, Quotation | None | \`risk-events\` |
| **9** | \`underwriting-service\`| 8087 | \`underwriting_db\`| Policy eligibility approval, risk grade assignment, premium loadings| Quotation, Risk | \`risk-events\` | \`underwriting-events\` |
| **10**| \`policy-service\` | 8088 | \`policy_db\` | Policy lifecycle (DRAFT, ISSUED, ACTIVE, EXPIRED, CANCELLED) | Customer, Product, Quotation, Payment, Premium | \`underwriting-events\`, \`policy-events\` (Saga) | \`policy-events\` |
| **11**| \`premium-service\` | 8089 | \`premium_db\` | Premium billing schedules, installment due dates, overdue tracking | Product, Quotation | \`policy-events\` | \`premium-events\` |
| **12**| \`payment-service\` | 8090 | \`payment_db\` | Payment intent creation, payment simulation, idempotency, receipts | None | None | \`payment-events\` |
| **13**| \`provider-service\` | 8091 | \`provider_db\` | Hospital/clinic network registry, active licensing, specialties | None | None | None |
| **14**| \`claims-service\` | 8092 | \`claims_db\` | 6-stage automated claim adjudication, deductible/copay computation | Policy, Provider | None | \`claim-events\` |
| **15**| \`document-service\` | 8093 | \`document_db\` | File upload metadata, storage path tracking, policy document links | None | None | None |
| **16**| \`notification-service\`| 8094 | \`notification_db\`| Notification logging for claims, policies, and premium events | None | \`policy-events\`, \`claim-events\`, \`premium-events\` | None |
| **17**| \`reporting-service\` | 8095 | \`reporting_db\` | Read-optimized analytics, aggregated claim settlement, KPIs | None | \`policy-events\`, \`claim-events\`, \`premium-events\` | None |

---
`;
  },

  getSection4: function() {
    return `
# 4. SERVICE-BY-SERVICE EXPLANATION

This section provides an exhaustive inspection of all 17 microservices in the system, detailing their business purpose, database tables, primary REST APIs, inter-service interactions, validation logic, and real-world execution scenarios.

---

## 4.1 Discovery & Infrastructure Services

### 4.1.1 Eureka Server (\`eureka-server\`)
- **Port**: 8761
- **Purpose**: Serves as the central Service Registry where all microservice instances dynamically register their host, port, health status, and metadata upon startup.
- **Responsibilities**:
  - Maintains a real-time registry of live service instances.
  - Receives periodic heartbeats (every 30 seconds) from client services.
  - Automatically evicts instances that fail to send heartbeats within the renewal threshold (90 seconds).
  - Enables client-side load balancing via Spring Cloud OpenFeign and Spring Cloud Gateway using service IDs (e.g., \`http://policy-service\`).
- **Dashboard**: Accessible at \`http://localhost:8761\` displaying registered instances and operational metrics.

### 4.1.2 Config Server (\`config-server\`)
- **Port**: 8888
- **Purpose**: Provides centralized, externalized configuration management across all environments (development, testing, production).
- **Responsibilities**:
  - Serves environment-specific configuration YAML files located in \`config-repo/\`.
  - Delivers database URLs, Kafka broker endpoints, JWT secrets, and port bindings to bootloader contexts.
  - Prevents secret hardcoding inside individual service source code repositories.

### 4.1.3 API Gateway (\`api-gateway\`)
- **Port**: 8080
- **Purpose**: Acts as the single, hardened reverse-proxy entry point for all external traffic originating from the Angular UI or third-party consumers.
- **Responsibilities**:
  - **Dynamic Routing**: Uses Eureka Discovery Locator (\`discovery.locator.enabled: true\`) to resolve routes such as \`/api/policies/**\` -> \`lb://policy-service\`.
  - **CORS Handling**: Globally manages Preflight (\`OPTIONS\`) requests, allowed headers (\`Authorization\`, \`Content-Type\`), allowed origins (\`http://localhost:4200\`), and credentials.
  - **Stateless Authentication Validation**: Validates the presence and signature of the \`Bearer <JWT>\` header before forwarding traffic to downstream business services.

---

## 4.2 Core Business Domain Services

### 4.2.1 Identity Service (\`identity-service\`)
- **Port**: 8081 | **Database**: \`identity_db\`
- **Purpose**: Provides centralized user identity management, credential authentication, role assignment, and cryptographically signed JWT token generation.
- **Database Schema**:
  - \`users\`: \`id\` (BIGINT PK), \`username\` (VARCHAR UNIQUE), \`email\` (VARCHAR UNIQUE), \`password\` (VARCHAR - BCrypt hashed), \`first_name\`, \`last_name\`, \`enabled\` (BOOLEAN), \`created_at\`, \`updated_at\`.
  - \`roles\`: \`id\` (BIGINT PK), \`name\` (VARCHAR UNIQUE - e.g., \`ROLE_CUSTOMER\`, \`ROLE_AGENT\`, \`ROLE_UNDERWRITER\`, \`ROLE_CLAIMS_OFFICER\`, \`ROLE_ADMIN\`).
  - \`user_roles\`: \`user_id\` (FK), \`role_id\` (FK) composite join table.
- **Key REST APIs**:
  - \`POST /api/auth/register\`: Validates user registration payload, hashes the raw password via \`BCryptPasswordEncoder\`, assigns default roles, and persists the user record.
  - \`POST /api/auth/login\`: Validates credentials against \`identity_db\`, creates a signed JWT containing username, user ID, and authority claims with 24-hour expiration.
  - \`POST /api/auth/refresh\`: Generates a renewed JWT token given a valid unexpired refresh token.
- **Security & Business Logic**:
  - Passwords are never stored in plaintext (enforced by BCrypt salt factor 10).
  - Returns standardized \`AuthResponse\` containing the JWT token, expiration timestamp, username, and assigned role set.

---

### 4.2.2 Customer Service (\`customer-service\`)
- **Port**: 8083 | **Database**: \`customer_db\`
- **Purpose**: Manages the master policyholder directory, demographic profiles, KYC status, contact information, and nominee declarations.
- **Database Schema**:
  - \`customers\`: \`id\` (BIGINT PK), \`customer_code\` (VARCHAR UNIQUE), \`first_name\`, \`last_name\`, \`email\` (VARCHAR UNIQUE), \`phone\` (VARCHAR), \`date_of_birth\` (DATE), \`gender\`, \`pan_number\`, \`aadhaar_number\`, \`kyc_status\` (\`PENDING\`, \`VERIFIED\`, \`REJECTED\`), \`created_at\`, \`updated_at\`.
  - \`customer_addresses\`: \`id\` (BIGINT PK), \`customer_id\` (FK), \`address_type\` (\`PERMANENT\`, \`COMMUNICATION\`), \`street\`, \`city\`, \`state\`, \`postal_code\`, \`country\`.
  - \`customer_nominees\`: \`id\` (BIGINT PK), \`customer_id\` (FK), \`nominee_name\`, \`relationship\`, \`date_of_birth\`, \`allocation_percentage\`.
- **Key REST APIs**:
  - \`POST /api/customers\`: Onboards a new customer, validates uniqueness of email and phone, generates unique \`CUST-YYYYMMDD-XXXX\` code, and stores address and nominee records.
  - \`GET /api/customers/{id}\`: Retrieves full customer profile including addresses and nominees.
  - \`GET /api/customers/code/{customerCode}\`: Lookup used by Policy and Quotation services.
  - \`PUT /api/customers/{id}/kyc\`: Updates KYC verification status.
- **Service Communications**:
  - Called synchronously by \`quotation-service\`, \`risk-service\`, and \`policy-service\` via OpenFeign to verify customer existence and demographic age.

---

### 4.2.3 Product & Plan Service (\`product-plan-service\`)
- **Port**: 8084 | **Database**: \`product_db\`
- **Purpose**: Acts as the centralized catalog of insurance products, insurance plans, coverage benefit limits, deductible structures, and age eligibility rules.
- **Database Schema**:
  - \`products\`: \`id\` (BIGINT PK), \`product_code\` (VARCHAR UNIQUE), \`product_name\`, \`product_type\` (\`INDIVIDUAL\`, \`FAMILY_FLOATER\`, \`SENIOR_CITIZEN\`, \`CRITICAL_ILLNESS\`), \`description\`, \`is_active\` (BOOLEAN).
  - \`plans\`: \`id\` (BIGINT PK), \`product_id\` (FK), \`plan_code\` (VARCHAR UNIQUE), \`plan_name\`, \`sum_insured\` (DECIMAL), \`base_premium\` (DECIMAL), \`min_age\`, \`max_age\`, \`is_active\` (BOOLEAN).
  - \`plan_benefits\`: \`id\` (BIGINT PK), \`plan_id\` (FK), \`benefit_name\`, \`benefit_code\`, \`limit_amount\` (DECIMAL), \`waiting_period_days\` (INT), \`copay_percentage\` (DECIMAL).
- **Key REST APIs**:
  - \`GET /api/products\`: Returns all active insurance products.
  - \`GET /api/plans\`: Returns all available plans with optional filtering by \`productId\`.
  - \`GET /api/plans/{id}\`: Retrieves detailed plan metadata including benefit limits, waiting periods, and copay percentages.
  - \`POST /api/products\`: Admin endpoint to create new insurance offerings.
- **Service Communications**:
  - Called synchronously by \`quotation-service\`, \`policy-service\`, and \`premium-service\` via OpenFeign.

---

### 4.2.4 Quotation Service (\`quotation-service\`)
- **Port**: 8085 | **Database**: \`quotation_db\`
- **Purpose**: Calculates actuarial premium estimates based on applicant age, selected sum insured, tobacco consumption, family member count, and optional add-on covers.
- **Database Schema**:
  - \`quotes\`: \`id\` (BIGINT PK), \`quote_number\` (VARCHAR UNIQUE), \`customer_id\` (BIGINT), \`plan_id\` (BIGINT), \`sum_insured\` (DECIMAL), \`base_premium\` (DECIMAL), \`tax_amount\` (DECIMAL), \`total_premium\` (DECIMAL), \`quote_status\` (\`GENERATED\`, \`ACCEPTED\`, \`EXPIRED\`, \`CONVERTED\`), \`expiry_date\` (DATE), \`created_at\`.
  - \`quote_members\`: \`id\` (BIGINT PK), \`quote_id\` (FK), \`relationship\`, \`age\`, \`gender\`, \`tobacco_user\` (BOOLEAN).
- **Key REST APIs**:
  - \`POST /api/quotes\`: Generates a personalized quote. Performs OpenFeign calls to \`CustomerClient\` and \`ProductClient\` to validate age and base rates, applies actuarial loading formulas, computes 18% GST/tax, and persists quote with a 30-day validity window.
  - \`GET /api/quotes/{id}\`: Returns quote summary.
  - \`GET /api/quotes/number/{quoteNumber}\`: Used by Underwriting and Policy services to inspect agreed terms.
  - \`PUT /api/quotes/{id}/accept\`: Transitions quote status to \`ACCEPTED\`.
- **Actuarial Pricing Formula Implemented**:
  \`Total Premium = Base Premium * AgeFactor * TobaccoFactor * FamilyDiscount + AddOnSum + GST (18%)\`

---

### 4.2.5 Risk Assessment Service (\`risk-service\`)
- **Port**: 8086 | **Database**: \`risk_db\`
- **Purpose**: Analyzes applicant health metrics, body mass index (BMI), declared pre-existing diseases, family medical history, and lifestyle factors to generate an objective numerical risk score.
- **Database Schema**:
  - \`risk_assessments\`: \`id\` (BIGINT PK), \`assessment_number\` (VARCHAR UNIQUE), \`quote_id\` (BIGINT), \`customer_id\` (BIGINT), \`risk_score\` (INT 0-100), \`risk_grade\` (\`LOW\`, \`MEDIUM\`, \`HIGH\`, \`DECLINED\`), \`recommended_loading_pct\` (DECIMAL), \`assessment_status\` (\`COMPLETED\`, \`PENDING_MEDICAL\`), \`created_at\`.
  - \`risk_factors\`: \`id\` (BIGINT PK), \`assessment_id\` (FK), \`factor_name\`, \`factor_type\` (\`BMI\`, \`TOBACCO\`, \`PRE_EXISTING\`, \`AGE\`), \`score_impact\` (INT), \`details\`.
- **Key REST APIs**:
  - \`POST /api/risk-assessments\`: Accepts applicant medical disclosures, evaluates BMI and pre-existing conditions, calculates composite risk score, assigns risk grade, and records score impacts.
  - \`GET /api/risk-assessments/quote/{quoteId}\`: Retrieves risk assessment associated with a specific quote.
- **Business Logic**:
  - Score 0–30: \`LOW\` (Standard terms, 0% loading).
  - Score 31–60: \`MEDIUM\` (Standard terms with 10%–25% loading).
  - Score 61–85: \`HIGH\` (Escalated to senior underwriter; 30%–50% loading or exclusions).
  - Score > 85: \`DECLINED\` (Automated rejection).

---

### 4.2.6 Underwriting Service (\`underwriting-service\`)
- **Port**: 8087 | **Database**: \`underwriting_db\`
- **Purpose**: Enforces underwriting guidelines, reviews risk assessments, determines final policy insurability, applies premium loading adjustments, and authorizes policy issuance.
- **Database Schema**:
  - \`underwriting_cases\`: \`id\` (BIGINT PK), \`case_number\` (VARCHAR UNIQUE), \`quote_id\` (BIGINT), \`risk_assessment_id\` (BIGINT), \`status\` (\`APPROVED\`, \`REJECTED\`, \`PENDING_REVIEW\`, \`REFERRED\`), \`approved_by\` (VARCHAR), \`premium_loading_percentage\` (DECIMAL), \`special_conditions\` (TEXT), \`rejection_reason\` (VARCHAR), \`decided_at\` (TIMESTAMP).
- **Key REST APIs**:
  - \`POST /api/underwriting/evaluate\`: Automates the underwriting decision by calling \`RiskAssessmentClient\` and \`QuotationClient\`. If risk grade is \`LOW\`, automatically approves the case; otherwise queues for underwriter review.
  - \`PUT /api/underwriting/{id}/approve\`: Manual approval by authorized underwriter (\`ROLE_UNDERWRITER\`).
  - \`PUT /api/underwriting/{id}/reject\`: Rejection with mandatory justification.
- **Kafka Integration**:
  - Emits \`UnderwritingApprovedEvent\` to Kafka topic \`underwriting-events\`.

---

### 4.2.7 Policy Service (\`policy-service\`)
- **Port**: 8088 | **Database**: \`policy_db\`
- **Purpose**: Serves as the central system of record for policy lifecycle governance, covering policy generation, policy issuance, endorsement, renewal, and cancellation.
- **Database Schema**:
  - \`policies\`: \`id\` (BIGINT PK), \`policy_number\` (VARCHAR UNIQUE), \`customer_id\` (BIGINT), \`plan_id\` (BIGINT), \`quote_id\` (BIGINT), \`underwriting_case_id\` (BIGINT), \`status\` (\`DRAFT\`, \`ISSUED\`, \`ACTIVE\`, \`EXPIRED\`, \`CANCELLED\`), \`start_date\` (DATE), \`end_date\` (DATE), \`sum_insured\` (DECIMAL), \`total_premium\` (DECIMAL), \`version\` (INT - Optimistic Locking), \`created_at\`, \`updated_at\`.
  - \`policy_members\`: \`id\` (BIGINT PK), \`policy_id\` (FK), \`member_name\`, \`relationship\`, \`date_of_birth\`, \`sum_insured_share\` (DECIMAL).
  - \`outbox_events\`: \`id\` (BIGINT PK), \`aggregate_type\` (VARCHAR), \`aggregate_id\` (VARCHAR), \`event_type\` (VARCHAR), \`payload\` (TEXT), \`status\` (\`PENDING\`, \`PUBLISHED\`, \`FAILED\`), \`retry_count\` (INT), \`created_at\`.
- **Key REST APIs**:
  - \`POST /api/policies/issue\`: Converts an approved quote and underwriting case into an official policy. Persists policy in \`ISSUED\` status and writes a \`PolicyIssuedEvent\` into the transactional \`outbox_events\` table within the same ACID database transaction.
  - \`GET /api/policies/{id}\`: Returns full policy details.
  - \`GET /api/policies/number/{policyNumber}\`: Critical endpoint consumed synchronously by \`claims-service\` during claim validation.
  - \`PUT /api/policies/{id}/activate\`: Moves policy from \`ISSUED\` to \`ACTIVE\` upon receipt of initial premium payment.
- **Transactional Outbox Engine**:
  - Uses \`PolicyOutboxPublisher\` scheduled task (every 5000ms) to read \`PENDING\` outbox records and relay them to Kafka topic \`policy-events\`.

---

### 4.2.8 Premium Service (\`premium-service\`)
- **Port**: 8089 | **Database**: \`premium_db\`
- **Purpose**: Generates and manages the installment billing schedule (Annual, Semi-Annual, Quarterly, Monthly), calculates due dates, grace periods, late fees, and reconciles paid installments.
- **Database Schema**:
  - \`premium_schedules\`: \`id\` (BIGINT PK), \`policy_id\` (BIGINT UNIQUE), \`policy_number\` (VARCHAR), \`payment_frequency\` (\`ANNUAL\`, \`SEMI_ANNUAL\`, \`QUARTERLY\`, \`MONTHLY\`), \`total_installments\` (INT), \`total_amount\` (DECIMAL), \`paid_amount\` (DECIMAL), \`balance_amount\` (DECIMAL), \`status\` (\`ACTIVE\`, \`COMPLETED\`, \`DEFAULTED\`).
  - \`installments\`: \`id\` (BIGINT PK), \`schedule_id\` (FK), \`installment_number\` (INT), \`due_date\` (DATE), \`amount\` (DECIMAL), \`grace_period_end_date\` (DATE), \`status\` (\`PENDING\`, \`PAID\`, \`OVERDUE\`), \`paid_at\` (TIMESTAMP).
- **Key REST APIs**:
  - \`POST /api/premium-schedules/generate\`: Generates the installment schedule based on policy terms.
  - \`GET /api/premium-schedules/policy/{policyId}\`: Retrieves schedule and installment breakdown.
  - \`PUT /api/premium-schedules/installments/{installmentId}/pay\`: Marks installment as paid upon payment confirmation.
- **Kafka Consumers**:
  - \`PolicyIssuedConsumer\`: Listens to \`policy-events\`. Automatically generates the initial premium schedule upon policy issuance. If an error occurs, emits \`PremiumScheduleFailedEvent\` triggering compensation.

---

### 4.2.9 Payment Service (\`payment-service\`)
- **Port**: 8090 | **Database**: \`payment_db\`
- **Purpose**: Handles payment intent creation, digital payment gateway simulation (Cards, NetBanking, UPI), transaction reconciliation, idempotent payment processing, and refund disbursements.
- **Database Schema**:
  - \`payments\`: \`id\` (BIGINT PK), \`transaction_reference\` (VARCHAR UNIQUE), \`policy_id\` (BIGINT), \`installment_id\` (BIGINT), \`amount\` (DECIMAL), \`payment_method\` (\`CREDIT_CARD\`, \`DEBIT_CARD\`, \`NET_BANKING\`, \`UPI\`), \`payment_status\` (\`INITIATED\`, \`SUCCESS\`, \`FAILED\`, \`REFUNDED\`), \`gateway_response_code\` (VARCHAR), \`created_at\`, \`updated_at\`.
  - \`idempotency_records\`: \`id\` (BIGINT PK), \`idempotency_key\` (VARCHAR UNIQUE), \`request_hash\` (VARCHAR), \`response_body\` (TEXT), \`status\` (\`PROCESSING\`, \`COMPLETED\`), \`created_at\`.
  - \`outbox_events\`: Transactional outbox table for reliable Kafka dispatch.
- **Key REST APIs**:
  - \`POST /api/payments/process\`: Processes payment with \`X-Idempotency-Key\` header validation. Simulates gateway approval, updates payment status, records transaction, and generates \`PremiumPaidEvent\` in the outbox.
  - \`GET /api/payments/{id}\`: Returns receipt metadata.
- **Idempotency Safeguard**:
  - Prevents double-charging if a user clicks "Pay" multiple times or network timeouts cause client retries.

---

### 4.2.10 Provider Service (\`provider-service\`)
- **Port**: 8091 | **Database**: \`provider_db\`
- **Purpose**: Manages the network of affiliated healthcare providers (hospitals, diagnostic clinics, specialty centers), monitors accreditation and state licensing status, and categorizes tier status (Tier 1 Preferred, Tier 2 Standard).
- **Database Schema**:
  - \`providers\`: \`id\` (BIGINT PK), \`provider_code\` (VARCHAR UNIQUE), \`provider_name\` (VARCHAR), \`provider_type\` (\`HOSPITAL\`, \`CLINIC\`, \`DIAGNOSTIC_CENTER\`), \`license_number\` (VARCHAR UNIQUE), \`network_status\` (\`IN_NETWORK\`, \`OUT_OF_NETWORK\`, \`SUSPENDED\`), \`tier\` (\`TIER_1\`, \`TIER_2\`, \`TIER_3\`), \`discount_percentage\` (DECIMAL), \`is_active\` (BOOLEAN), \`created_at\`.
  - \`provider_departments\`: \`id\` (BIGINT PK), \`provider_id\` (FK), \`department_name\` (e.g. \`CARDIOLOGY\`, \`ONCOLOGY\`, \`GENERAL_SURGERY\`), \`contact_phone\`.
- **Key REST APIs**:
  - \`GET /api/providers\`: Lists healthcare providers with search filters (city, network status, department).
  - \`GET /api/providers/{id}\`: Retrieves provider profile.
  - \`GET /api/providers/code/{providerCode}\`: Crucial endpoint called synchronously by \`claims-service\` during automated adjudication.
  - \`POST /api/providers\`: Admin endpoint to onboard hospitals.
- **Business Logic**:
  - When claims are submitted for an \`IN_NETWORK\` provider, the patient is eligible for direct cashless settlement. For \`OUT_OF_NETWORK\`, reimbursement rules apply with higher copay.

---

### 4.2.11 Claims Service (\`claims-service\`)
- **Port**: 8092 | **Database**: \`claims_db\`
- **Purpose**: The most sophisticated domain engine in HIMS. Executes a deterministic, 6-stage automated claim adjudication workflow, evaluates policy coverage limits, verifies waiting periods, computes deductibles and copays, and produces Explanations of Benefits (EOB).
- **Database Schema (11 Tables)**:
  - \`claims\`: \`id\` (BIGINT PK), \`claim_number\` (VARCHAR UNIQUE), \`policy_number\` (VARCHAR), \`customer_id\` (BIGINT), \`provider_code\` (VARCHAR), \`claim_type\` (\`CASHLESS\`, \`REIMBURSEMENT\`), \`admission_date\` (DATE), \`discharge_date\` (DATE), \`claimed_amount\` (DECIMAL), \`approved_amount\` (DECIMAL), \`deductible_amount\` (DECIMAL), \`copay_amount\` (DECIMAL), \`settlement_status\` (\`SUBMITTED\`, \`IN_REVIEW\`, \`VALIDATED\`, \`ADJUDICATED\`, \`APPROVED\`, \`REJECTED\`, \`SETTLED\`), \`rejection_reason\` (VARCHAR), \`created_at\`, \`updated_at\`.
  - \`claim_diagnoses\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`icd10_code\` (VARCHAR), \`description\` (VARCHAR), \`is_primary\` (BOOLEAN).
  - \`claim_services\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`service_date\` (DATE), \`service_code\` (VARCHAR), \`service_description\` (VARCHAR), \`billed_amount\` (DECIMAL), \`allowed_amount\` (DECIMAL).
  - \`claim_documents\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`document_id\` (BIGINT), \`document_type\` (\`HOSPITAL_BILL\`, \`DISCHARGE_SUMMARY\`, \`PRESCRIPTION\`), \`verified\` (BOOLEAN).
  - \`claim_validations\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`validation_stage\` (VARCHAR), \`rule_name\` (VARCHAR), \`passed\` (BOOLEAN), \`details\` (VARCHAR).
  - \`claim_adjudications\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`total_billed\` (DECIMAL), \`disallowed_amount\` (DECIMAL), \`deductible_applied\` (DECIMAL), \`copay_applied\` (DECIMAL), \`net_approved\` (DECIMAL), \`adjudicated_by\` (VARCHAR), \`adjudicated_at\` (TIMESTAMP).
  - \`explanation_of_benefits\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`eob_number\` (VARCHAR UNIQUE), \`patient_responsibility\` (DECIMAL), \`insurer_paid\` (DECIMAL), \`notes\` (TEXT), \`generated_at\` (TIMESTAMP).
  - \`claim_payments\`: \`id\` (BIGINT PK), \`claim_id\` (FK), \`payment_reference\` (VARCHAR), \`payment_amount\` (DECIMAL), \`disbursement_status\` (\`PENDING\`, \`DISBURSED\`), \`disbursed_at\` (TIMESTAMP).
  - \`idempotency_records\`: Unique request deduplication store.
  - \`outbox_events\`: Transactional outbox table for \`claim-events\` Kafka topic.
  - \`audit_records\`: Full entity audit log tracking state changes.
- **Key REST APIs**:
  - \`POST /api/claims\`: Submits new claim. Validates idempotency, persists claim in \`SUBMITTED\` state, and triggers automated validation.
  - \`POST /api/claims/{id}/adjudicate\`: Executes the multi-stage adjudication engine.
  - \`GET /api/claims/{id}\`: Returns full claim dossier including services, diagnoses, validation stages, and EOB.
  - \`PUT /api/claims/{id}/approve\` / \`reject\`: Officer manual override.
- **Automated 6-Stage Adjudication Engine**:
  1. *Format & Completeness*: Checks required bills, dates, and non-empty service items.
  2. *Policy Eligibility*: Synchronously queries \`PolicyServiceClient\`. Confirms policy exists, is in \`ACTIVE\` status, and admission date falls within \`start_date\` and \`end_date\`.
  3. *Provider Network Verification*: Synchronously queries \`ProviderServiceClient\`. Checks license validity and network standing.
  4. *Benefit & Waiting Period*: Verifies whether the diagnosed ICD-10 condition is covered or subject to a 30-day / 2-year pre-existing waiting period.
  5. *Financial Mathematics*: Calculates non-covered expenses, applies plan deductible (e.g. $500), applies copay percentage (e.g. 10%), and arrives at net payable:
     \`Net Approved = Max(0, (Billed Amount - Disallowed - Deductible) * (1 - Copay%))\`
  6. *Settlement Authorization*: Emits \`ClaimApprovedEvent\` or \`ClaimRejectedEvent\` to Kafka.

---

### 4.2.12 Document Service (\`document-service\`)
- **Port**: 8093 | **Database**: \`document_db\`
- **Purpose**: Manages file uploads, metadata indexing, storage paths, and download links for KYC IDs, hospital invoices, medical reports, and generated policy PDFs.
- **Database Schema**:
  - \`documents\`: \`id\` (BIGINT PK), \`document_code\` (VARCHAR UNIQUE), \`file_name\` (VARCHAR), \`file_type\` (VARCHAR - e.g. \`application/pdf\`, \`image/jpeg\`), \`file_size\` (BIGINT), \`storage_path\` (VARCHAR), \`entity_type\` (\`CUSTOMER\`, \`POLICY\`, \`CLAIM\`), \`entity_id\` (BIGINT), \`uploaded_at\` (TIMESTAMP).
- **Key REST APIs**:
  - \`POST /api/documents/upload\`: Multipart file upload handler. Validates file extension, computes MD5 checksum, saves to local disk/storage, and persists metadata.
  - \`GET /api/documents/{id}/download\`: Serves document byte stream with \`Content-Disposition\` headers.

---

### 4.2.13 Notification Service (\`notification-service\`)
- **Port**: 8094 | **Database**: \`notification_db\`
- **Purpose**: Consumes domain events from Kafka and dispatches automated simulated notifications (Email, SMS) to customers and administrators regarding policy issuances, payments, and claim updates.
- **Database Schema**:
  - \`notifications\`: \`id\` (BIGINT PK), \`recipient_email\` (VARCHAR), \`recipient_phone\` (VARCHAR), \`notification_type\` (\`EMAIL\`, \`SMS\`), \`subject\` (VARCHAR), \`message_body\` (TEXT), \`event_source\` (VARCHAR), \`delivery_status\` (\`SENT\`, \`FAILED\`), \`sent_at\` (TIMESTAMP).
- **Kafka Consumers**:
  - \`PolicyEventConsumer\`: Listens to \`policy-events\`; sends "Policy Issued" and "Policy Activated" notifications.
  - \`ClaimEventConsumer\`: Listens to \`claim-events\`; sends "Claim Received", "Claim Approved", and "Claim Rejected" updates.
  - \`PremiumEventConsumer\`: Listens to \`premium-events\`; sends "Premium Receipt" and payment due alerts.

---

### 4.2.14 Reporting Service (\`reporting-service\`)
- **Port**: 8095 | **Database**: \`reporting_db\`
- **Purpose**: Provides a decoupled, read-optimized data warehouse supporting business intelligence, executive dashboard KPIs, loss ratio calculations, and monthly claim distribution metrics.
- **Database Schema**:
  - \`daily_kpi_summaries\`: \`id\` (BIGINT PK), \`summary_date\` (DATE UNIQUE), \`total_policies_issued\` (INT), \`total_premium_collected\` (DECIMAL), \`total_claims_submitted\` (INT), \`total_claims_approved\` (INT), \`total_claims_amount_paid\` (DECIMAL), \`loss_ratio\` (DECIMAL).
  - \`claim_analytics\`: \`id\` (BIGINT PK), \`claim_number\` (VARCHAR), \`policy_number\` (VARCHAR), \`provider_code\` (VARCHAR), \`claimed_amount\` (DECIMAL), \`approved_amount\` (DECIMAL), \`adjudication_time_seconds\` (INT).
- **Kafka Consumers**:
  - Consumes from \`policy-events\`, \`premium-events\`, and \`claim-events\` to update summary tables in real time without placing query load on primary transaction databases.
- **Key REST APIs**:
  - \`GET /api/reports/dashboard-kpis\`: Serves aggregated performance figures to the Angular admin dashboard.

---
`;
  }
};
