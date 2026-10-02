const fs = require('fs');
const path = require('path');

const docDir = path.join(__dirname, '..', 'documentation');
if (!fs.existsSync(docDir)) {
  fs.mkdirSync(docDir, { recursive: true });
}

// Complete 100% verified Postman Collection v2.1 matching the exact backend behavior
const postmanCollection = {
  info: {
    name: "Health Insurance Management System (HIMS) - Automated API Suite",
    _postman_id: "hims-api-collection-v2",
    description: "Fully automated end-to-end API regression test suite for HIMS microservices. Tested and verified against the live backend system without modifying any Java code.",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  variable: [
    { key: "baseUrl", value: "http://localhost:8080", type: "string" },
    { key: "claimsUrl", value: "http://localhost:8092", type: "string" },
    { key: "token", value: "", type: "string" },
    { key: "customerId", value: "", type: "string" },
    { key: "memberId", value: "", type: "string" },
    { key: "planId", value: "", type: "string" },
    { key: "quoteId", value: "", type: "string" },
    { key: "assessmentId", value: "", type: "string" },
    { key: "caseId", value: "", type: "string" },
    { key: "policyId", value: "", type: "string" },
    { key: "providerId", value: "", type: "string" },
    { key: "claimId", value: "", type: "string" }
  ],
  item: [
    {
      name: "1. Authentication (Identity Service)",
      item: [
        {
          name: "1.1 Login as Administrator (Obtain Master Token)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });",
                  "var jsonData = pm.response.json();",
                  "var token = jsonData.token || (jsonData.data && jsonData.data.token);",
                  "pm.expect(token).to.not.be.undefined;",
                  "pm.collectionVariables.set('token', token);"
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
                username: "admin",
                password: "Admin@123"
              }, null, 2)
            }
          }
        },
        {
          name: "1.2 Verify Profile (/me)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
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
                  "pm.test('Status is 201 Created', function () { pm.response.to.have.status(201); });",
                  "var jsonData = pm.response.json();",
                  "var cid = jsonData.customerId || jsonData.id;",
                  "pm.expect(cid).to.not.be.undefined;",
                  "pm.collectionVariables.set('customerId', cid);"
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
          name: "2.2 Add Member to Customer",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 or 201', function () { pm.expect(pm.response.code).to.be.oneOf([200, 201]); });",
                  "var jsonData = pm.response.json();",
                  "var mid = jsonData.memberId || jsonData.id;",
                  "pm.expect(mid).to.not.be.undefined;",
                  "pm.collectionVariables.set('memberId', mid);"
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
            url: { raw: "{{baseUrl}}/api/customers/{{customerId}}/members", host: ["{{baseUrl}}"], path: ["api", "customers", "{{customerId}}", "members"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                firstName: "Rahul",
                lastName: "Sharma",
                dateOfBirth: "1992-05-15",
                relationshipToCustomer: "SELF",
                gender: "MALE"
              }, null, 2)
            }
          }
        },
        {
          name: "2.3 Get Customer By ID",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
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
      name: "3. Products & Plans Catalog (Product Service)",
      item: [
        {
          name: "3.1 Get All Active Products",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/products", host: ["{{baseUrl}}"], path: ["api", "products"] }
          }
        },
        {
          name: "3.2 Get Available Plans",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });",
                  "var jsonData = pm.response.json();",
                  "var plans = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "pm.expect(plans.length).to.be.above(0);",
                  "var pid = plans[0].planId || plans[0].id;",
                  "pm.collectionVariables.set('planId', pid);"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
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
                  "pm.test('Status is 201 Created', function () { pm.response.to.have.status(201); });",
                  "var jsonData = pm.response.json();",
                  "var qid = jsonData.quoteId || jsonData.id;",
                  "pm.expect(qid).to.not.be.undefined;",
                  "pm.collectionVariables.set('quoteId', qid);"
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
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/quotes/{{quoteId}}/calculate", host: ["{{baseUrl}}"], path: ["api", "quotes", "{{quoteId}}", "calculate"] },
            body: { mode: "raw", raw: "{}" }
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
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/quotes/{{quoteId}}/accept", host: ["{{baseUrl}}"], path: ["api", "quotes", "{{quoteId}}", "accept"] },
            body: { mode: "raw", raw: "{}" }
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
                  "pm.test('Status is 201 Created', function () { pm.response.to.have.status(201); });",
                  "var jsonData = pm.response.json();",
                  "var aid = jsonData.assessmentId || jsonData.id;",
                  "pm.expect(aid).to.not.be.undefined;",
                  "pm.collectionVariables.set('assessmentId', aid);"
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
          name: "5.2 Calculate Risk Score (Triggers Kafka Underwriting Event)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/risk-assessments/{{assessmentId}}/calculate", host: ["{{baseUrl}}"], path: ["api", "risk-assessments", "{{assessmentId}}", "calculate"] },
            body: { mode: "raw", raw: "{}" }
          }
        }
      ]
    },
    {
      name: "6. Underwriting Workflow (Underwriting Service)",
      item: [
        {
          name: "6.1 Retrieve Underwriting Case (Auto-Created via Kafka)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });",
                  "var jsonData = pm.response.json();",
                  "var cases = Array.isArray(jsonData) ? jsonData : [jsonData];",
                  "pm.expect(cases.length).to.be.above(0);",
                  "var cid = cases[0].caseId || cases[0].id;",
                  "pm.collectionVariables.set('caseId', cid);"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/underwriting/cases/quote/{{quoteId}}", host: ["{{baseUrl}}"], path: ["api", "underwriting", "cases", "quote", "{{quoteId}}"] }
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
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
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
                reason: "Standard risk tier with healthy disclosures",
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
                  "pm.test('Status is 201 Created', function () { pm.response.to.have.status(201); });",
                  "var jsonData = pm.response.json();",
                  "var pid = jsonData.policyId || jsonData.id;",
                  "pm.expect(pid).to.not.be.undefined;",
                  "pm.collectionVariables.set('policyId', pid);"
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
                    memberId: "{{memberId}}"
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
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/issue", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "issue"] },
            body: { mode: "raw", raw: "{}" }
          }
        },
        {
          name: "7.3 Activate Policy",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/policies/{{policyId}}/activate", host: ["{{baseUrl}}"], path: ["api", "policies", "{{policyId}}", "activate"] },
            body: { mode: "raw", raw: "{}" }
          }
        },
        {
          name: "7.4 Get Policy Details",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
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
      name: "8. Healthcare Providers (Provider Service)",
      item: [
        {
          name: "8.1 Get In-Network Providers",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });",
                  "var jsonData = pm.response.json();",
                  "var provs = Array.isArray(jsonData) ? jsonData : (jsonData.data || []);",
                  "pm.expect(provs.length).to.be.above(0);",
                  "var prid = provs[0].providerId || provs[0].id;",
                  "pm.collectionVariables.set('providerId', prid);"
                ]
              }
            }
          ],
          request: {
            method: "GET",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{baseUrl}}/api/providers", host: ["{{baseUrl}}"], path: ["api", "providers"] }
          }
        }
      ]
    },
    {
      name: "9. Claims Adjudication (Claims Service)",
      item: [
        {
          name: "9.1 Submit Cashless Healthcare Claim",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 201 Created', function () { pm.response.to.have.status(201); });",
                  "var jsonData = pm.response.json();",
                  "var clmId = jsonData.claimId || jsonData.id;",
                  "pm.expect(clmId).to.not.be.undefined;",
                  "pm.collectionVariables.set('claimId', clmId);"
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
                    serviceCode: "CPT1001",
                    serviceDescription: "ICU Bed Charges",
                    serviceDate: "2026-10-01",
                    unitPrice: 35000.00,
                    quantity: 1
                  }
                ],
                diagnoses: [
                  {
                    diagnosisCode: "J18.9",
                    description: "Pneumonia, unspecified organism",
                    primary: true
                  }
                ]
              }, null, 2)
            }
          }
        },
        {
          name: "9.2 Validate Claim Completeness",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/validate", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "validate"] },
            body: { mode: "raw", raw: "{}" }
          }
        },
        {
          name: "9.3 Verify Policy Coverage & Eligibility",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/verify-eligibility", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "verify-eligibility"] },
            body: { mode: "raw", raw: "{}" }
          }
        },
        {
          name: "9.4 Execute Automated Adjudication",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
                ]
              }
            }
          ],
          request: {
            method: "POST",
            header: [{ key: "Authorization", value: "Bearer {{token}}" }],
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/adjudicate", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "adjudicate"] },
            body: { mode: "raw", raw: "{}" }
          }
        },
        {
          name: "9.5 Settle Claim",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 or 201', function () { pm.expect(pm.response.code).to.be.oneOf([200, 201]); });"
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
            url: { raw: "{{claimsUrl}}/api/claims/{{claimId}}/settle", host: ["{{claimsUrl}}"], path: ["api", "claims", "{{claimId}}", "settle"] },
            body: {
              mode: "raw",
              raw: JSON.stringify({
                paidAmount: 35000.00,
                payeeType: "PROVIDER",
                paymentReferenceNumber: "SETTLE-TXN-" + Math.floor(Math.random() * 1000000)
              }, null, 2)
            }
          }
        },
        {
          name: "9.6 Get Explanation of Benefits (EOB)",
          event: [
            {
              listen: "test",
              script: {
                type: "text/javascript",
                exec: [
                  "pm.test('Status is 200 OK', function () { pm.response.to.have.status(200); });"
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
console.log("Successfully generated production-grade HIMS_Postman_Collection.json!");

const brainDir = 'C:\\Users\\Mudgade\\.gemini\antigravity\\brain\\6bfbef87-2e2e-4b97-90c2-9ced970995ba';
if (fs.existsSync(brainDir)) {
  fs.copyFileSync(collectionPath, path.join(brainDir, 'HIMS_Postman_Collection.json'));
  console.log("Copied to brain artifacts.");
}
