const fs = require('fs');
const path = require('path');

const docDir = path.join(__dirname, '..', 'documentation');
if (!fs.existsSync(docDir)) {
  fs.mkdirSync(docDir, { recursive: true });
}

// 1. Postman Collection v2.1 schema
const postmanCollection = {
  info: {
    name: "Health Insurance Management System (HIMS) - API Collection",
    _postman_id: "hims-api-collection-v1",
    description: "Complete sequential end-to-end API test collection for HIMS microservices. Designed for demo presentations and guide verification.",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  variable: [
    { key: "baseUrl", value: "http://localhost:8080", type: "string" },
    { key: "token", value: "", type: "string" },
    { key: "customerId", value: "", type: "string" },
    { key: "planId", value: "", type: "string" },
    { key: "quoteId", value: "", type: "string" },
    { key: "assessmentId", value: "", type: "string" },
    { key: "caseId", value: "", type: "string" },
    { key: "policyId", value: "", type: "string" },
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
          request: {
            method: "POST",
            header: [{ key: "Content-Type", value: "application/json" }],
            url: { raw: "{{baseUrl}}/api/auth/register", host: ["{{baseUrl}}"], path: ["api", "auth", "register"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                username: "demo_user",
                password: "Password123!",
                email: "demo_user@hims.com",
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
                  "var jsonData = pm.response.json();",
                  "if (jsonData.token) {",
                  "    pm.collectionVariables.set('token', jsonData.token);",
                  "} else if (jsonData.data && jsonData.data.token) {",
                  "    pm.collectionVariables.set('token', jsonData.data.token);",
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
                  "var jsonData = pm.response.json();",
                  "var cid = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (cid) pm.collectionVariables.set('customerId', cid);"
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
                identificationNumber: "ABCDE1234F"
              }, null, 2)
            }
          }
        },
        {
          name: "2.2 Get Customer By ID",
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
                  "var jsonData = pm.response.json();",
                  "var plans = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "if (plans.length > 0 && plans[0].id) {",
                  "    pm.collectionVariables.set('planId', plans[0].id);",
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
                  "var jsonData = pm.response.json();",
                  "var qid = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (qid) pm.collectionVariables.set('quoteId', qid);"
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
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/quotes/{{quoteId}}/calculate", host: ["{{baseUrl}}"], path: ["api", "quotes", "{{quoteId}}", "calculate"] }
          }
        },
        {
          name: "4.3 Accept Quote",
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
                  "var jsonData = pm.response.json();",
                  "var aid = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (aid) pm.collectionVariables.set('assessmentId', aid);"
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
                  "var jsonData = pm.response.json();",
                  "var cid = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (cid) pm.collectionVariables.set('caseId', cid);"
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
                  "var jsonData = pm.response.json();",
                  "var pid = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (pid) pm.collectionVariables.set('policyId', pid);"
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
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/issue", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "issue"] }
          }
        },
        {
          name: "7.3 Get Policy Details",
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
                  "var jsonData = pm.response.json();",
                  "var insts = jsonData.installments || (jsonData.data && jsonData.data.installments) || [];",
                  "if (insts.length > 0 && insts[0].id) {",
                  "    pm.collectionVariables.set('installmentId', insts[0].id);",
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
                  "var jsonData = pm.response.json();",
                  "var payId = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (payId) pm.collectionVariables.set('paymentId', payId);"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [
              { key: "Content-Type", value: "application/json" },
              { key: "Authorization", value: "Bearer {{token}}" },
              { key: "X-Idempotency-Key", value: "demo-pay-001" }
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
                gatewayReference: "MOCK-TXN-123456789",
                isSuccess: true,
                failureReason: null
              }, null, 2)
            }
          }
        },
        {
          name: "9.3 Activate Policy",
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
                  "var jsonData = pm.response.json();",
                  "var provs = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "if (provs.length > 0 && provs[0].id) {",
                  "    pm.collectionVariables.set('providerId', provs[0].id);",
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
                  "var jsonData = pm.response.json();",
                  "var clmId = jsonData.id || (jsonData.data && jsonData.data.id);",
                  "if (clmId) pm.collectionVariables.set('claimId', clmId);"
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
            url: { raw: "http://localhost:8092/api/claims", host: ["http://localhost:8092"], path: ["api", "claims"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                policyId: "{{policyId}}",
                memberId: "00000000-0000-0000-0000-000000000001",
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
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "http://localhost:8092/api/claims/{{claimId}}/validate", host: ["http://localhost:8092"], path: ["api", "claims", "{{claimId}}", "validate"] }
          }
        },
        {
          name: "11.3 Verify Policy Coverage & Eligibility",
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "http://localhost:8092/api/claims/{{claimId}}/verify-eligibility", host: ["http://localhost:8092"], path: ["api", "claims", "{{claimId}}", "verify-eligibility"] }
          }
        },
        {
          name: "11.4 Execute Automated Adjudication",
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "http://localhost:8092/api/claims/{{claimId}}/adjudicate", host: ["http://localhost:8092"], path: ["api", "claims", "{{claimId}}", "adjudicate"] }
          }
        },
        {
          name: "11.5 Get Explanation of Benefits (EOB)",
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "http://localhost:8092/api/claims/{{claimId}}/eob", host: ["http://localhost:8092"], path: ["api", "claims", "{{claimId}}", "eob"] }
          }
        }
      ]
    }
  ]
};

const collectionPath = path.join(docDir, 'HIMS_Postman_Collection.json');
fs.writeFileSync(collectionPath, JSON.stringify(postmanCollection, null, 2), 'utf8');
console.log("Saved Postman Collection to:", collectionPath);

// Copy to brain artifacts
const brainDir = 'C:\\Users\\Mudgade\\.gemini\\antigravity\\brain\\6bfbef87-2e2e-4b97-90c2-9ced970995ba';
if (fs.existsSync(brainDir)) {
  fs.copyFileSync(collectionPath, path.join(brainDir, 'HIMS_Postman_Collection.json'));
  console.log("Copied Postman Collection to brain artifacts.");
}

console.log("Assets created successfully.");
