const fs = require('fs');
const path = require('path');

const docDir = path.join(__dirname, '..', 'documentation');
if (!fs.existsSync(docDir)) {
  fs.mkdirSync(docDir, { recursive: true });
}

// 1. Postman Collection v2.1 schema with robust DTO field extractions
const postmanCollection = {
  info: {
    name: "Health Insurance Management System (HIMS) - API Collection",
    _postman_id: "hims-api-collection-v1",
    description: "Complete sequential end-to-end API test collection for HIMS microservices. Designed for automated testing via Postman Collection Runner.",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  variable: [
    { key: "baseUrl", value: "http://localhost:8080", type: "string" },
    { key: "claimsUrl", value: "http://localhost:8092", type: "string" },
    { key: "token", value: "", type: "string" },
    { key: "customerId", value: "", type: "string" },
    { key: "planId", value: "", type: "string" },
    { key: "quoteId", value: "", type: "string" },
    { key: "assessmentId", value: "", type: "string" },
    { key: "caseId", value: "", type: "string" },
    { key: "policyId", value: "", type: "string" },
    { key: "memberId", value: "", type: "string" },
    { key: "installmentId", value: "", type: "string" },
    { key: "paymentId", value: "", type: "string" },
    { key: "providerId", value: "", type: "string" },
    { key: "claimId", value: "", type: "string" }
  ],
  item: [
    {
      name: "1. Authentication & Security (Identity Service)",
      item: [
        {
          name: "1.1 Register New User",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 or 201', function () {",
                  "    pm.expect(pm.response.code).to.be.oneOf([200, 201]);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            url: { raw: "{{baseUrl}}/api/auth/register", host: ["{{baseUrl}}"], path: ["api", "auth", "register"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                username: "demo_user_" + Math.floor(Math.random() * 10000),
                password: "Password123!",
                email: "demo_" + Math.floor(Math.random() * 10000) + "@hims.com",
                role: "CUSTOMER"
              }, null, 2)
            }
          }
        },
        {
          name: "1.2 Login & Obtain JWT Token",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Login Successful (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "var token = jsonData.token || (jsonData.data && jsonData.data.token);",
                  "if (token) {",
                  "    pm.collectionVariables.set('token', token);",
                  "    console.log('Saved JWT Token successfully');",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            url: { raw: "{{baseUrl}}/api/auth/login", host: ["{{baseUrl}}"], path: ["api", "auth", "login"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                username: "demo_user",
                password: "Password123!"
              }, null, 2)
            }
          }
        },
        {
          name: "1.3 Get Current User Profile (/me)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Profile Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/auth/me", host: ["{{baseUrl}}"], path: ["api", "auth", "me"] }
          }
        }
      ]
    },
    {
      name: "2. Customer Management (Customer Service)",
      item: [
        {
          name: "2.1 Create Customer Profile",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Customer Created (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// CustomerResponse returns customerId",
                  "var cid = jsonData.customerId || jsonData.id || (jsonData.data && (jsonData.data.customerId || jsonData.data.id));",
                  "if (cid) {",
                  "    pm.collectionVariables.set('customerId', cid);",
                  "    console.log('Saved customerId: ' + cid);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/customers", host: ["{{baseUrl}}"], path: ["api", "customers"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                firstName: "Rahul",
                lastName: "Sharma",
                dateOfBirth: "1992-05-15",
                gender: "MALE",
                identificationNumber: "PAN" + Math.floor(Math.random() * 1000000)
              }, null, 2)
            }
          }
        },
        {
          name: "2.2 Get Customer By ID",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Customer Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/customers/{{customerId}}", host: ["{{baseUrl}}"], path: ["api", "customers", "{{customerId}}"] }
          }
        }
      ]
    },
    {
      name: "3. Product & Plans Catalog (Product Service)",
      item: [
        {
          name: "3.1 Get All Active Products",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Products Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [],
            url: { raw: "{{baseUrl}}/api/products", host: ["{{baseUrl}}"], path: ["api", "products"] }
          }
        },
        {
          name: "3.2 Get All Available Plans",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Plans Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "var plans = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "if (plans.length > 0) {",
                  "    // PlanResponse returns planId",
                  "    var pid = plans[0].planId || plans[0].id;",
                  "    if (pid) {",
                  "        pm.collectionVariables.set('planId', pid);",
                  "        console.log('Saved planId: ' + pid);",
                  "    }",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [],
            url: { raw: "{{baseUrl}}/api/plans", host: ["{{baseUrl}}"], path: ["api", "plans"] }
          }
        }
      ]
    },
    {
      name: "4. Quotation Engine (Quotation Service)",
      item: [
        {
          name: "4.1 Create Quote",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Quote Created (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// QuoteResponse returns quoteId",
                  "var qid = jsonData.quoteId || jsonData.id || (jsonData.data && (jsonData.data.quoteId || jsonData.data.id));",
                  "if (qid) {",
                  "    pm.collectionVariables.set('quoteId', qid);",
                  "    console.log('Saved quoteId: ' + qid);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/quotes", host: ["{{baseUrl}}"], path: ["api", "quotes"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                customerId: "{{customerId}}",
                planId: "{{planId}}",
                members: [
                  {
                    memberName: "Rahul Sharma",
                    dateOfBirth: "1992-05-15",
                    relationship: "SELF",
                    gender: "MALE"
                  }
                ]
              }, null, 2)
            }
          }
        },
        {
          name: "4.2 Calculate Quote Premium",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Premium Calculated (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/quotes/{{quoteId}}/calculate", host: ["{{baseUrl}}"], path: ["api", "quotes", "{{quoteId}}", "calculate"] }
          }
        },
        {
          name: "4.3 Accept Quote",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Quote Accepted (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/quotes/{{quoteId}}/accept", host: ["{{baseUrl}}"], path: ["api", "quotes", "{{quoteId}}", "accept"] }
          }
        }
      ]
    },
    {
      name: "5. Risk Assessment (Risk Service)",
      item: [
        {
          name: "5.1 Create Health Risk Assessment",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Risk Assessment Created (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// RiskAssessmentResponse returns assessmentId",
                  "var aid = jsonData.assessmentId || jsonData.id || (jsonData.data && (jsonData.data.assessmentId || jsonData.data.id));",
                  "if (aid) {",
                  "    pm.collectionVariables.set('assessmentId', aid);",
                  "    console.log('Saved assessmentId: ' + aid);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/risk-assessments", host: ["{{baseUrl}}"], path: ["api", "risk-assessments"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                customerId: "{{customerId}}",
                quoteId: "{{quoteId}}",
                factors: [
                  { factorName: "Age", factorValue: "34", description: "Applicant age" },
                  { factorName: "Smoker", factorValue: "NO", description: "Tobacco usage" },
                  { factorName: "BMI", factorValue: "23.5", description: "Normal BMI range" }
                ]
              }, null, 2)
            }
          }
        },
        {
          name: "5.2 Calculate Risk Score",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Risk Calculated (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/risk-assessments/{{assessmentId}}/calculate", host: ["{{baseUrl}}"], path: ["api", "risk-assessments", "{{assessmentId}}", "calculate"] }
          }
        }
      ]
    },
    {
      name: "6. Underwriting Workflow (Underwriting Service)",
      item: [
        {
          name: "6.1 Create Underwriting Case",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Case Created (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// UnderwritingCaseResponse returns caseId",
                  "var cid = jsonData.caseId || jsonData.id || (jsonData.data && (jsonData.data.caseId || jsonData.data.id));",
                  "if (cid) {",
                  "    pm.collectionVariables.set('caseId', cid);",
                  "    console.log('Saved caseId: ' + cid);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/underwriting/cases", host: ["{{baseUrl}}"], path: ["api", "underwriting", "cases"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                quoteId: "{{quoteId}}",
                customerId: "{{customerId}}",
                assessmentId: "{{assessmentId}}"
              }, null, 2)
            }
          }
        },
        {
          name: "6.2 Approve Underwriting Case",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Underwriting Approved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/underwriting/cases/{{caseId}}/approve", host: ["{{baseUrl}}"], path: ["api", "underwriting", "cases", "{{caseId}}", "approve"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                decisionType: "APPROVED",
                reason: "Standard risk tier with healthy biometric disclosures",
                notes: "Certified for policy binding"
              }, null, 2)
            }
          }
        }
      ]
    },
    {
      name: "7. Policy Lifecycle (Policy Service)",
      item: [
        {
          name: "7.1 Create Policy Draft",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Policy Created (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// PolicyResponse returns policyId",
                  "var pid = jsonData.policyId || jsonData.id || (jsonData.data && (jsonData.data.policyId || jsonData.data.id));",
                  "if (pid) {",
                  "    pm.collectionVariables.set('policyId', pid);",
                  "    console.log('Saved policyId: ' + pid);",
                  "}",
                  "if (jsonData.members && jsonData.members.length > 0) {",
                  "    var mid = jsonData.members[0].memberId || jsonData.members[0].policyMemberId;",
                  "    if (mid) pm.collectionVariables.set('memberId', mid);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/policies", host: ["{{baseUrl}}"], path: ["api", "policies"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                customerId: "{{customerId}}",
                quoteId: "{{quoteId}}",
                planId: "{{planId}}",
                effectiveDate: "2026-10-01T00:00:00Z",
                expiryDate: "2027-09-30T23:59:59Z",
                members: [
                  {
                    memberName: "Rahul Sharma",
                    dateOfBirth: "1992-05-15",
                    relationship: "SELF",
                    gender: "MALE"
                  }
                ]
              }, null, 2)
            }
          }
        },
        {
          name: "7.2 Issue Policy",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Policy Issued (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/issue", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "issue"] }
          }
        },
        {
          name: "7.3 Get Policy Details",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Policy Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}"] }
          }
        }
      ]
    },
    {
      name: "8. Premium Billing (Premium Service)",
      item: [
        {
          name: "8.1 Get Policy Premium Schedule",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Schedule Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "var insts = jsonData.installments || (jsonData.data && jsonData.data.installments) || [];",
                  "if (insts.length > 0) {",
                  "    var iid = insts[0].installmentId || insts[0].id;",
                  "    if (iid) {",
                  "        pm.collectionVariables.set('installmentId', iid);",
                  "        console.log('Saved installmentId: ' + iid);",
                  "    }",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/premium", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "premium"] }
          }
        }
      ]
    },
    {
      name: "9. Payment & Activation (Payment Service)",
      item: [
        {
          name: "9.1 Initiate Premium Payment",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Payment Initiated (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// PaymentResponse returns paymentId",
                  "var payId = jsonData.paymentId || jsonData.id || (jsonData.data && (jsonData.data.paymentId || jsonData.data.id));",
                  "if (payId) {",
                  "    pm.collectionVariables.set('paymentId', payId);",
                  "    console.log('Saved paymentId: ' + payId);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" },
              { key: "X-Idempotency-Key", value: "pay-" + Math.floor(Math.random() * 100000) }
            ],
            url: { raw: "{{baseUrl}}/api/payments", host: ["{{baseUrl}}"], path: ["api", "payments"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                policyId: "{{policyId}}",
                installmentId: "{{installmentId}}",
                amount: 14500.00,
                paymentMethod: "CREDIT_CARD"
              }, null, 2)
            }
          }
        },
        {
          name: "9.2 Confirm Payment",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Payment Confirmed (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{baseUrl}}/api/payments/{{paymentId}}/confirm", host: ["{{baseUrl}}"], path: ["api", "payments", "{{paymentId}}", "confirm"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                gatewayReference: "MOCK-TXN-" + Math.floor(Math.random() * 1000000),
                isSuccess: true,
                failureReason: null
              }, null, 2)
            }
          }
        },
        {
          name: "9.3 Activate Policy",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Policy Activated (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/activate", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "activate"] }
          }
        }
      ]
    },
    {
      name: "10. Healthcare Providers (Provider Service)",
      item: [
        {
          name: "10.1 Get All In-Network Providers",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Providers Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "var provs = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "if (provs.length > 0) {",
                  "    // ProviderResponse returns providerId",
                  "    var prid = provs[0].providerId || provs[0].id;",
                  "    if (prid) {",
                  "        pm.collectionVariables.set('providerId', prid);",
                  "        console.log('Saved providerId: ' + prid);",
                  "    }",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [],
            url: { raw: "{{baseUrl}}/api/providers", host: ["{{baseUrl}}"], path: ["api", "providers"] }
          }
        }
      ]
    },
    {
      name: "11. Claims Adjudication (Claims Service)",
      item: [
        {
          name: "11.1 Submit Cashless Healthcare Claim",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Claim Submitted (Status 201)', function () {",
                  "    pm.response.to.have.status(201);",
                  "});",
                  "var jsonData = pm.response.json();",
                  "// ClaimResponse returns claimId",
                  "var clmId = jsonData.claimId || jsonData.id || (jsonData.data && (jsonData.data.claimId || jsonData.data.id));",
                  "if (clmId) {",
                  "    pm.collectionVariables.set('claimId', clmId);",
                  "    console.log('Saved claimId: ' + clmId);",
                  "}"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" }
            ],
            url: { raw: "{{claimsUrl}}/api/claims", host: ["{{claimsUrl}}"], path: ["api", "claims"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                policyId: "{{policyId}}",
                memberId: "{{memberId}}",
                providerId: "{{providerId}}",
                claimType: "CASHLESS",
                serviceDate: "2026-10-02",
                admissionDate: "2026-10-01",
                dischargeDate: "2026-10-02",
                totalClaimAmount: 35000.00,
                remarks: "Acute pneumonia emergency admission",
                serviceLines: [
                  {
                    serviceCode: "BED-ICU",
                    serviceName: "ICU Bed Charges",
                    serviceCategory: "ROOM_CHARGE",
                    serviceDate: "2026-10-01",
                    quantity: 1,
                    unitPrice: 20000.00,
                    requestedAmount: 20000.00
                  },
                  {
                    serviceCode: "MED-IV",
                    serviceName: "Intravenous Antibiotics",
                    serviceCategory: "PHARMACY",
                    serviceDate: "2026-10-02",
                    quantity: 1,
                    unitPrice: 15000.00,
                    requestedAmount: 15000.00
                  }
                ],
                diagnoses: [
                  {
                    diagnosisCode: "J18.9",
                    diagnosisDescription: "Pneumonia, unspecified organism",
                    isPrimary: true
                  }
                ]
              }, null, 2)
            }
          }
        },
        {
          name: "11.2 Validate Claim Completeness",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Validation Successful (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/validate", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "validate"] }
          }
        },
        {
          name: "11.3 Verify Policy Coverage & Eligibility",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Eligibility Verified (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/verify-eligibility", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "verify-eligibility"] }
          }
        },
        {
          name: "11.4 Execute Automated Adjudication",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Adjudication Successful (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/adjudicate", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "adjudicate"] }
          }
        },
        {
          name: "11.5 Get Explanation of Benefits (EOB)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('EOB Retrieved (Status 200)', function () {",
                  "    pm.response.to.have.status(200);",
                  "});"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/eob", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "eob"] }
          }
        }
      ]
    }
  ]
};

const collectionPath = path.join(docDir, 'HIMS_Postman_Collection.json');
fs.writeFileSync(collectionPath, JSON.stringify(postmanCollection, null, 2), 'utf8');
console.log("Updated HIMS_Postman_Collection.json with exact DTO field mappings!");

const brainDir = 'C:\\Users\\Mudgade\\.gemini\\antigravity\\brain\\6bfbef87-2e2e-4b97-90c2-9ced970995ba';
if (fs.existsSync(brainDir)) {
  fs.copyFileSync(collectionPath, path.join(brainDir, 'HIMS_Postman_Collection.json'));
  console.log("Copied to brain artifacts.");
}
