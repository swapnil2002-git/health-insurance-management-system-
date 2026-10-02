# Health Insurance Management System (HIMS)
# Complete Postman & Swagger UI Testing Guide

This guide provides the complete, mentor-ready testing directory for all **Swagger UI** interfaces and a sequential, step-by-step **Postman testing workflow** with pre-filled, schema-accurate JSON test data.

---

## 1. Swagger UI & OpenAPI Directory

Every microservice exposes an interactive **Swagger UI** on its dedicated port using `springdoc-openapi-starter-webmvc-ui`. You can test APIs directly from your web browser without Postman.

### Direct Service Swagger UI URLs

| # | Microservice Name | Port | Direct Swagger UI URL | OpenAPI JSON Spec |
| :--- | :--- | :--- | :--- | :--- |
| **1** | **Identity Service** | `8081` | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | `http://localhost:8081/v3/api-docs` |
| **2** | **Customer Service** | `8083` | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) | `http://localhost:8083/v3/api-docs` |
| **3** | **Product & Plan Service** | `8084` | [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) | `http://localhost:8084/v3/api-docs` |
| **4** | **Quotation Service** | `8085` | [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html) | `http://localhost:8085/v3/api-docs` |
| **5** | **Risk Assessment Service** | `8086` | [http://localhost:8086/swagger-ui/index.html](http://localhost:8086/swagger-ui/index.html) | `http://localhost:8086/v3/api-docs` |
| **6** | **Underwriting Service** | `8087` | [http://localhost:8087/swagger-ui/index.html](http://localhost:8087/swagger-ui/index.html) | `http://localhost:8087/v3/api-docs` |
| **7** | **Policy Service** | `8088` | [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html) | `http://localhost:8088/v3/api-docs` |
| **8** | **Premium Service** | `8089` | [http://localhost:8089/swagger-ui/index.html](http://localhost:8089/swagger-ui/index.html) | `http://localhost:8089/v3/api-docs` |
| **9** | **Payment Service** | `8090` | [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html) | `http://localhost:8090/v3/api-docs` |
| **10**| **Provider Service** | `8091` | [http://localhost:8091/swagger-ui/index.html](http://localhost:8091/swagger-ui/index.html) | `http://localhost:8091/v3/api-docs` |
| **11**| **Claims Service** | `8092` | [http://localhost:8092/swagger-ui/index.html](http://localhost:8092/swagger-ui/index.html) | `http://localhost:8092/v3/api-docs` |
| **12**| **Document Service** | `8093` | [http://localhost:8093/swagger-ui/index.html](http://localhost:8093/swagger-ui/index.html) | `http://localhost:8093/v3/api-docs` |
| **13**| **Notification Service** | `8094` | [http://localhost:8094/swagger-ui/index.html](http://localhost:8094/swagger-ui/index.html) | `http://localhost:8094/v3/api-docs` |
| **14**| **Reporting Service** | `8095` | [http://localhost:8095/swagger-ui/index.html](http://localhost:8095/swagger-ui/index.html) | `http://localhost:8095/v3/api-docs` |

> [!TIP]
> **How to Authorize in Swagger UI**:
> 1. Log in via `POST /api/auth/login` on Identity Service (Port 8081) and copy the returned `token`.
> 2. On any Swagger UI page, click the green **`Authorize`** button (top right).
> 3. Enter: `Bearer <YOUR_COPIED_TOKEN>` (or just paste the token if `Bearer` is pre-fixed).
> 4. Click **Authorize** -> **Close**. You can now test secured endpoints!

---

## 2. 1-Click Postman Collection Import

A complete Postman Collection file has been generated for you:
📁 **File Path**: `documentation/HIMS_Postman_Collection.json`
👉 [Click to Open / Copy Postman Collection](file:///C:/Users/Mudgade/.gemini/antigravity/scratch/health-insurance-management-system/documentation/HIMS_Postman_Collection.json)

### How to Import into Postman:
1. Open **Postman**.
2. Click the **"Import"** button (top left).
3. Drag & drop `HIMS_Postman_Collection.json` into Postman.
4. The entire collection with **11 folders**, environment variables, and pre-configured JSON bodies will load automatically!

---

## 3. Complete Step-by-Step API Testing Flow

Execute these requests in this exact sequence. You can route them through the **API Gateway** (`http://localhost:8080`) or directly to the service ports.

---

### Step 1: User Registration & Authentication (Identity Service)

#### 1.1 Register User
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/register` (or `http://localhost:8081/api/auth/register`)
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "username": "demo_user",
  "password": "Password123!",
  "email": "demo_user@hims.com",
  "role": "CUSTOMER"
}
```
- **Expected Status**: `201 Created` or `200 OK`

#### 1.2 Login & Get JWT Token
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/login` (or `http://localhost:8081/api/auth/login`)
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "username": "demo_user",
  "password": "Password123!"
}
```
- **Expected Status**: `200 OK`
- **Response**: Copy the `token` string from the JSON response.
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "demo_user",
    "roles": ["ROLE_CUSTOMER"]
  }
  ```
> **Action**: For all subsequent calls, add the header:
> `Authorization: Bearer <YOUR_TOKEN>`

---

### Step 2: Customer Onboarding (Customer Service)

#### 2.1 Create Customer Profile
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/customers` (or `http://localhost:8083/api/customers`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "dateOfBirth": "1992-05-15",
  "gender": "MALE",
  "identificationNumber": "ABCDE1234F"
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id` (e.g., `"c4b8e219-9f7a-4c91-9e23-7a912c0199bc"`). This is your `customerId`.

---

### Step 3: Browse Plans (Product Service)

#### 3.1 Get Available Plans
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/plans` (or `http://localhost:8084/api/plans`)
- **Expected Status**: `200 OK`
- **Response**: Copy any plan's `id` (e.g., `"plan-id-uuid"`). This is your `planId`.

---

### Step 4: Quotation Generation (Quotation Service)

#### 4.1 Create Quote
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/quotes` (or `http://localhost:8085/api/quotes`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "customerId": "<YOUR_CUSTOMER_ID>",
  "planId": "<YOUR_PLAN_ID>",
  "members": [
    {
      "memberName": "Rahul Sharma",
      "dateOfBirth": "1992-05-15",
      "relationship": "SELF",
      "gender": "MALE"
    }
  ]
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `quoteId`.

#### 4.2 Calculate Quote Premium
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/quotes/<YOUR_QUOTE_ID>/calculate` (or `http://localhost:8085/...`)
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- **Response**: Shows base premium, tax amount, and total calculated premium.

---

### Step 5: Medical Risk Assessment (Risk Service)

#### 5.1 Submit Health Questionnaire
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/risk-assessments` (or `http://localhost:8086/api/risk-assessments`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "customerId": "<YOUR_CUSTOMER_ID>",
  "quoteId": "<YOUR_QUOTE_ID>",
  "factors": [
    { "factorName": "Age", "factorValue": "34", "description": "Applicant age" },
    { "factorName": "Smoker", "factorValue": "NO", "description": "Tobacco user" },
    { "factorName": "BMI", "factorValue": "23.5", "description": "Body Mass Index" }
  ]
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `assessmentId`.

#### 5.2 Calculate Risk Score
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/risk-assessments/<YOUR_ASSESSMENT_ID>/calculate`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- **Response**: Returns risk score (e.g. `18 / 100`) and risk classification `LOW`.

---

### Step 6: Underwriting Approval (Underwriting Service)

#### 6.1 Create Underwriting Case
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/underwriting/cases` (or `http://localhost:8087/api/underwriting/cases`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "quoteId": "<YOUR_QUOTE_ID>",
  "customerId": "<YOUR_CUSTOMER_ID>",
  "assessmentId": "<YOUR_ASSESSMENT_ID>"
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `caseId`.

#### 6.2 Approve Case
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/underwriting/cases/<YOUR_CASE_ID>/approve`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "decisionType": "APPROVED",
  "reason": "Standard risk tier with healthy biometric disclosures",
  "notes": "Certified for policy binding"
}
```
- **Expected Status**: `200 OK`

---

### Step 7: Policy Creation & Issuance (Policy Service)

#### 7.1 Create Policy Draft
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/policies` (or `http://localhost:8088/api/policies`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "customerId": "<YOUR_CUSTOMER_ID>",
  "quoteId": "<YOUR_QUOTE_ID>",
  "planId": "<YOUR_PLAN_ID>",
  "effectiveDate": "2026-10-01T00:00:00Z",
  "expiryDate": "2027-09-30T23:59:59Z",
  "members": [
    {
      "memberName": "Rahul Sharma",
      "dateOfBirth": "1992-05-15",
      "relationship": "SELF",
      "gender": "MALE"
    }
  ]
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `policyId`.

#### 7.2 Issue Policy
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/policies/<YOUR_POLICY_ID>/issue`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- *Note*: Policy status transitions to `ISSUED`. Transactional outbox emits `PolicyIssuedEvent` to Kafka `policy-events`.

---

### Step 8: View Premium Schedule (Premium Service)

#### 8.1 Get Installment Breakdown
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/policies/<YOUR_POLICY_ID>/premium` (or `http://localhost:8089/...`)
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- **Response**: Copy the `id` of the first installment. This is your `installmentId`.

---

### Step 9: Make Payment & Activate Policy (Payment Service)

#### 9.1 Initiate Premium Payment
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/payments` (or `http://localhost:8090/api/payments`)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
  - `X-Idempotency-Key`: `pay-test-key-001`
- **Request Body**:
```json
{
  "policyId": "<YOUR_POLICY_ID>",
  "installmentId": "<YOUR_INSTALLMENT_ID>",
  "amount": 14500.00,
  "paymentMethod": "CREDIT_CARD"
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `paymentId`.

#### 9.2 Confirm Payment Gateway
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/payments/<YOUR_PAYMENT_ID>/confirm`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "gatewayReference": "TXN-MOCK-987654321",
  "isSuccess": true,
  "failureReason": null
}
```
- **Expected Status**: `200 OK`

#### 9.3 Activate Policy
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/policies/<YOUR_POLICY_ID>/activate`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- *Note*: Policy status is now **`ACTIVE`**! The customer is eligible for hospital coverage.

---

### Step 10: Provider Network (Provider Service)

#### 10.1 Get In-Network Hospital
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/providers` (or `http://localhost:8091/api/providers`)
- **Expected Status**: `200 OK`
- **Response**: Copy any provider's `id`. This is your `providerId`.

---

### Step 11: Claim Submission & 6-Stage Adjudication (Claims Service)

#### 11.1 Submit Cashless Healthcare Claim
- **Method**: `POST`
- **URL**: `http://localhost:8092/api/claims`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <YOUR_TOKEN>`
- **Request Body**:
```json
{
  "policyId": "<YOUR_POLICY_ID>",
  "memberId": "00000000-0000-0000-0000-000000000001",
  "providerId": "<YOUR_PROVIDER_ID>",
  "claimType": "CASHLESS",
  "serviceDate": "2026-10-02",
  "admissionDate": "2026-10-01",
  "dischargeDate": "2026-10-02",
  "totalClaimAmount": 35000.00,
  "remarks": "Acute pneumonia emergency treatment",
  "serviceLines": [
    {
      "serviceCode": "BED-ICU",
      "serviceName": "ICU Bed Charges",
      "serviceCategory": "ROOM_CHARGE",
      "serviceDate": "2026-10-01",
      "quantity": 1,
      "unitPrice": 20000.00,
      "requestedAmount": 20000.00
    },
    {
      "serviceCode": "MED-IV",
      "serviceName": "IV Antibiotic Treatment",
      "serviceCategory": "PHARMACY",
      "serviceDate": "2026-10-02",
      "quantity": 1,
      "unitPrice": 15000.00,
      "requestedAmount": 15000.00
    }
  ],
  "diagnoses": [
    {
      "diagnosisCode": "J18.9",
      "diagnosisDescription": "Pneumonia, unspecified organism",
      "isPrimary": true
    }
  ]
}
```
- **Expected Status**: `201 Created`
- **Response**: Copy the returned `id`. This is your `claimId`.

#### 11.2 Validate Claim Completeness
- **Method**: `POST`
- **URL**: `http://localhost:8092/api/claims/<YOUR_CLAIM_ID>/validate`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK` (Validation checks format and line item coherence).

#### 11.3 Verify Policy Coverage & Eligibility
- **Method**: `POST`
- **URL**: `http://localhost:8092/api/claims/<YOUR_CLAIM_ID>/verify-eligibility`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK` (Calls Policy Service via OpenFeign; verifies policy is `ACTIVE`).

#### 11.4 Execute Automated Adjudication
- **Method**: `POST`
- **URL**: `http://localhost:8092/api/claims/<YOUR_CLAIM_ID>/adjudicate`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- *Note*: Evaluates benefits, calculates deductible and copay, determines net approved amount.

#### 11.5 Get Explanation of Benefits (EOB)
- **Method**: `GET`
- **URL**: `http://localhost:8092/api/claims/<YOUR_CLAIM_ID>/eob`
- **Headers**: `Authorization: Bearer <YOUR_TOKEN>`
- **Expected Status**: `200 OK`
- **Response**: Returns the complete formal accounting breakdown of insurer paid vs patient responsibility!
