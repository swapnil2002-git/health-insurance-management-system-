import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { AuthGuard } from './core/auth/auth.guard';
import { RoleGuard } from './core/auth/role.guard';

// Module 3: User Management Components
import { UserRegisterComponent } from './features/user-management/user-register/user-register.component';
import { UserAuthorizeComponent } from './features/user-management/user-authorize/user-authorize.component';
import { UserRoleComponent } from './features/user-management/user-role/user-role.component';

// Module 4: Customer Components
import { PersonalInfoComponent } from './features/customer/personal-info/personal-info.component';
import { MemberDetailsComponent } from './features/customer/member-details/member-details.component';
import { BeneficiaryDetailsComponent } from './features/customer/beneficiary-details/beneficiary-details.component';
import { ContactDetailsComponent } from './features/customer/contact-details/contact-details.component';
import { AddressDetailsComponent } from './features/customer/address-details/address-details.component';
import { NomineeDetailsComponent } from './features/customer/nominee-details/nominee-details.component';

// Module 5: Product & Plan Components
import { ProductListComponent } from './features/product-plan/product-list/product-list.component';
import { PlanListComponent } from './features/product-plan/plan-list/plan-list.component';
import { PlanRuleMappingComponent } from './features/product-plan/plan-rule-mapping/plan-rule-mapping.component';
import { MasterRulesComponent } from './features/product-plan/master-rules/master-rules.component';

// Module 6: Quotation Components
import { CreateQuoteComponent } from './features/quotation/create-quote/create-quote.component';
import { CalculateQuoteComponent } from './features/quotation/calculate-quote/calculate-quote.component';
import { QuoteDecisionComponent } from './features/quotation/quote-decision/quote-decision.component';
import { QuoteViewComponent } from './features/quotation/quote-view/quote-view.component';
import { QuoteListComponent } from './features/quotation/quote-list/quote-list.component';

// Module 7: Risk Assessment Components
import { RiskAssessComponent } from './features/risk/risk-assess/risk-assess.component';
import { RiskCalculateComponent } from './features/risk/risk-calculate/risk-calculate.component';
import { RiskViewComponent } from './features/risk/risk-view/risk-view.component';

// Module 8: Underwriting Components
import { UnderwritingCasesComponent } from './features/underwriting/underwriting-cases/underwriting-cases.component';
import { UnderwritingApproveComponent } from './features/underwriting/underwriting-approve/underwriting-approve.component';
import { UnderwritingRejectComponent } from './features/underwriting/underwriting-reject/underwriting-reject.component';
import { UnderwritingReferComponent } from './features/underwriting/underwriting-refer/underwriting-refer.component';
import { UnderwritingViewComponent } from './features/underwriting/underwriting-view/underwriting-view.component';

// Module 9: Policy Components
import { PolicyListComponent } from './features/policy/policy-list/policy-list.component';
import { PolicyIssueComponent } from './features/policy/policy-issue/policy-issue.component';
import { PolicyViewComponent } from './features/policy/policy-view/policy-view.component';
import { PolicyEndorsementComponent } from './features/policy/policy-endorsement/policy-endorsement.component';
import { PolicyRenewalComponent } from './features/policy/policy-renewal/policy-renewal.component';
import { PolicyCancelComponent } from './features/policy/policy-cancel/policy-cancel.component';

// Module 10: Premium Components
import { PremiumScheduleComponent } from './features/premium/premium-schedule/premium-schedule.component';
import { PremiumRecalculateComponent } from './features/premium/premium-recalculate/premium-recalculate.component';
import { PremiumInstallmentComponent } from './features/premium/premium-installment/premium-installment.component';
import { PremiumViewComponent } from './features/premium/premium-view/premium-view.component';

// Module 11: Payment Components
import { PaymentInitiateComponent } from './features/payment/payment-initiate/payment-initiate.component';
import { PaymentConfirmComponent } from './features/payment/payment-confirm/payment-confirm.component';
import { PaymentRefundComponent } from './features/payment/payment-refund/payment-refund.component';
import { PaymentViewComponent } from './features/payment/payment-view/payment-view.component';
import { PaymentListComponent } from './features/payment/payment-list/payment-list.component';

// Module 12: Provider Components
import { ProviderCreateComponent } from './features/provider/provider-create/provider-create.component';
import { ProviderAddressComponent } from './features/provider/provider-address/provider-address.component';
import { ProviderViewComponent } from './features/provider/provider-view/provider-view.component';
import { ProviderListComponent } from './features/provider/provider-list/provider-list.component';
import { NetworkCreateComponent } from './features/provider/network-create/network-create.component';
import { NetworkMappingComponent } from './features/provider/network-mapping/network-mapping.component';
import { NetworkListComponent } from './features/provider/network-list/network-list.component';

// Module 13: Claim Components
import { ClaimListComponent } from './features/claim/claim-list/claim-list.component';
import { ClaimCreateComponent } from './features/claim/claim-create/claim-create.component';
import { ClaimValidateComponent } from './features/claim/claim-validate/claim-validate.component';
import { ClaimEligibilityComponent } from './features/claim/claim-eligibility/claim-eligibility.component';
import { ClaimAdjudicateComponent } from './features/claim/claim-adjudicate/claim-adjudicate.component';
import { ClaimSettleComponent } from './features/claim/claim-settle/claim-settle.component';
import { ClaimViewComponent } from './features/claim/claim-view/claim-view.component';
import { ClaimPaymentComponent } from './features/claim/claim-payment/claim-payment.component';

// Module 14: Document Components
import { DocumentUploadComponent } from './features/document/document-upload/document-upload.component';
import { DocumentViewComponent } from './features/document/document-view/document-view.component';
import { DocumentReferenceComponent } from './features/document/document-reference/document-reference.component';
import { DocumentListComponent } from './features/document/document-list/document-list.component';

// Module 15: Notification Components
import { NotificationTemplateListComponent } from './features/notification/template-list/template-list.component';
import { NotificationTemplateCreateComponent } from './features/notification/template-create/template-create.component';
import { NotificationTemplateViewComponent } from './features/notification/template-view/template-view.component';
import { NotificationSendComponent } from './features/notification/notification-send/notification-send.component';
import { NotificationViewComponent } from './features/notification/notification-view/notification-view.component';
import { NotificationReferenceComponent } from './features/notification/notification-reference/notification-reference.component';

// Module 16: Report Components
import { ReportSummaryComponent } from './features/report/report-summary/report-summary.component';
import { ReportPoliciesComponent } from './features/report/report-policies/report-policies.component';
import { ReportClaimsComponent } from './features/report/report-claims/report-claims.component';
import { ReportPremiumsComponent } from './features/report/report-premiums/report-premiums.component';

const customerRoles = ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const productPlanRoles = ['POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN', 'AGENT', 'CUSTOMER'];
const productAdminRoles = ['POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const quoteRoles = ['CUSTOMER', 'AGENT', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const riskRoles = ['UNDERWRITER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const underwritingRoles = ['UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const policyRoles = ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const policyAdminRoles = ['POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const premiumRoles = ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const premiumAdminRoles = ['FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const paymentRoles = ['CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const paymentRefundRoles = ['FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const providerRoles = ['HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const providerAdminRoles = ['POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const claimRoles = ['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const claimActionRoles = ['CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const claimSettleRoles = ['CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const documentRoles = ['CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const notificationTemplateRoles = ['POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const notificationTemplateAdminRoles = ['POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const notificationRoles = ['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const notificationSendRoles = ['POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'];
const reportRoles = ['SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'CLAIMS_OFFICER', 'UNDERWRITER'];
const reportPolicyRoles = ['SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'UNDERWRITER'];
const reportClaimRoles = ['SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'FINANCE_OFFICER'];
const reportPremiumRoles = ['SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER'];

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },

      // Module 3: User Management
      {
        path: 'users/register',
        component: UserRegisterComponent,
        canActivate: [RoleGuard],
        data: { roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN'] }
      },
      {
        path: 'users/authorize',
        component: UserAuthorizeComponent,
        canActivate: [RoleGuard],
        data: { roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN'] }
      },
      {
        path: 'users/role',
        component: UserRoleComponent,
        canActivate: [RoleGuard],
        data: { roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN'] }
      },

      // Module 4: Customer
      {
        path: 'customers/info',
        component: PersonalInfoComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      {
        path: 'customers/members',
        component: MemberDetailsComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      {
        path: 'customers/beneficiaries',
        component: BeneficiaryDetailsComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      {
        path: 'customers/contacts',
        component: ContactDetailsComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      {
        path: 'customers/addresses',
        component: AddressDetailsComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      {
        path: 'customers/nominees',
        component: NomineeDetailsComponent,
        canActivate: [RoleGuard],
        data: { roles: customerRoles }
      },
      { path: 'customers', redirectTo: 'customers/info', pathMatch: 'full' },

      // Module 5: Product & Plan
      {
        path: 'products',
        component: ProductListComponent,
        canActivate: [RoleGuard],
        data: { roles: productPlanRoles }
      },
      {
        path: 'plans',
        component: PlanListComponent,
        canActivate: [RoleGuard],
        data: { roles: productPlanRoles }
      },
      {
        path: 'plans/rule-mapping',
        component: PlanRuleMappingComponent,
        canActivate: [RoleGuard],
        data: { roles: productAdminRoles }
      },
      {
        path: 'plans/rules',
        component: MasterRulesComponent,
        canActivate: [RoleGuard],
        data: { roles: productPlanRoles }
      },

      // Module 6: Quotation
      {
        path: 'quotes/create',
        component: CreateQuoteComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },
      {
        path: 'quotes/calculate',
        component: CalculateQuoteComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },
      {
        path: 'quotes/decision',
        component: QuoteDecisionComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },
      {
        path: 'quotes/view/:quoteId',
        component: QuoteViewComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },
      {
        path: 'quotes/view',
        component: QuoteViewComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },
      {
        path: 'quotes',
        component: QuoteListComponent,
        canActivate: [RoleGuard],
        data: { roles: quoteRoles }
      },

      // Module 7: Risk Assessment
      {
        path: 'risk/assess',
        component: RiskAssessComponent,
        canActivate: [RoleGuard],
        data: { roles: riskRoles }
      },
      {
        path: 'risk/calculate',
        component: RiskCalculateComponent,
        canActivate: [RoleGuard],
        data: { roles: riskRoles }
      },
      {
        path: 'risk/view/:id',
        component: RiskViewComponent,
        canActivate: [RoleGuard],
        data: { roles: riskRoles }
      },
      {
        path: 'risk/view',
        component: RiskViewComponent,
        canActivate: [RoleGuard],
        data: { roles: riskRoles }
      },
      { path: 'risk', redirectTo: 'risk/assess', pathMatch: 'full' },

      // Module 8: Underwriting
      {
        path: 'underwriting/cases',
        component: UnderwritingCasesComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      {
        path: 'underwriting/approve',
        component: UnderwritingApproveComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      {
        path: 'underwriting/reject',
        component: UnderwritingRejectComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      {
        path: 'underwriting/refer',
        component: UnderwritingReferComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      {
        path: 'underwriting/view/:id',
        component: UnderwritingViewComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      {
        path: 'underwriting/view',
        component: UnderwritingViewComponent,
        canActivate: [RoleGuard],
        data: { roles: underwritingRoles }
      },
      { path: 'underwriting', redirectTo: 'underwriting/cases', pathMatch: 'full' },

      // Module 9: Policy
      {
        path: 'policies',
        component: PolicyListComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },
      {
        path: 'policies/issue',
        component: PolicyIssueComponent,
        canActivate: [RoleGuard],
        data: { roles: policyAdminRoles }
      },
      {
        path: 'policies/view/:id',
        component: PolicyViewComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },
      {
        path: 'policies/view',
        component: PolicyViewComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },
      {
        path: 'policies/endorse',
        component: PolicyEndorsementComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },
      {
        path: 'policies/renew',
        component: PolicyRenewalComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },
      {
        path: 'policies/cancel',
        component: PolicyCancelComponent,
        canActivate: [RoleGuard],
        data: { roles: policyRoles }
      },

      // Module 10: Premium Billing
      {
        path: 'premium/schedules',
        component: PremiumScheduleComponent,
        canActivate: [RoleGuard],
        data: { roles: premiumRoles }
      },
      {
        path: 'premium/recalculate',
        component: PremiumRecalculateComponent,
        canActivate: [RoleGuard],
        data: { roles: premiumAdminRoles }
      },
      {
        path: 'premium/installments',
        component: PremiumInstallmentComponent,
        canActivate: [RoleGuard],
        data: { roles: premiumRoles }
      },
      {
        path: 'premium/view/:policyId',
        component: PremiumViewComponent,
        canActivate: [RoleGuard],
        data: { roles: premiumRoles }
      },
      {
        path: 'premium/view',
        component: PremiumViewComponent,
        canActivate: [RoleGuard],
        data: { roles: premiumRoles }
      },
      { path: 'premium', redirectTo: 'premium/schedules', pathMatch: 'full' },

      // Module 11: Payment
      {
        path: 'payments',
        component: PaymentListComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRoles }
      },
      {
        path: 'payments/pay',
        component: PaymentInitiateComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRoles }
      },
      {
        path: 'payments/confirm',
        component: PaymentConfirmComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRoles }
      },
      {
        path: 'payments/refund',
        component: PaymentRefundComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRefundRoles }
      },
      {
        path: 'payments/view/:id',
        component: PaymentViewComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRoles }
      },
      {
        path: 'payments/view',
        component: PaymentViewComponent,
        canActivate: [RoleGuard],
        data: { roles: paymentRoles }
      },

      // Module 12: Provider
      {
        path: 'providers',
        component: ProviderListComponent,
        canActivate: [RoleGuard],
        data: { roles: providerRoles }
      },
      {
        path: 'providers/create',
        component: ProviderCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },
      {
        path: 'providers/edit/:id',
        component: ProviderCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },
      {
        path: 'providers/address',
        component: ProviderAddressComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },
      {
        path: 'providers/view/:id',
        component: ProviderViewComponent,
        canActivate: [RoleGuard],
        data: { roles: providerRoles }
      },
      {
        path: 'providers/view',
        component: ProviderViewComponent,
        canActivate: [RoleGuard],
        data: { roles: providerRoles }
      },
      {
        path: 'providers/networks',
        component: NetworkListComponent,
        canActivate: [RoleGuard],
        data: { roles: providerRoles }
      },
      {
        path: 'providers/networks/create',
        component: NetworkCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },
      {
        path: 'providers/networks/edit/:id',
        component: NetworkCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },
      {
        path: 'providers/networks/mapping',
        component: NetworkMappingComponent,
        canActivate: [RoleGuard],
        data: { roles: providerAdminRoles }
      },

      // Module 13: Claim
      {
        path: 'claims',
        component: ClaimListComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },
      {
        path: 'claims/create',
        component: ClaimCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },
      {
        path: 'claims/validate',
        component: ClaimValidateComponent,
        canActivate: [RoleGuard],
        data: { roles: claimActionRoles }
      },
      {
        path: 'claims/eligibility',
        component: ClaimEligibilityComponent,
        canActivate: [RoleGuard],
        data: { roles: claimActionRoles }
      },
      {
        path: 'claims/adjudicate',
        component: ClaimAdjudicateComponent,
        canActivate: [RoleGuard],
        data: { roles: claimActionRoles }
      },
      {
        path: 'claims/settle',
        component: ClaimSettleComponent,
        canActivate: [RoleGuard],
        data: { roles: claimSettleRoles }
      },
      {
        path: 'claims/view/:id',
        component: ClaimViewComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },
      {
        path: 'claims/view',
        component: ClaimViewComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },
      {
        path: 'claims/payment/:id',
        component: ClaimPaymentComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },
      {
        path: 'claims/payment',
        component: ClaimPaymentComponent,
        canActivate: [RoleGuard],
        data: { roles: claimRoles }
      },

      // Module 14: Document
      {
        path: 'documents',
        component: DocumentListComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },
      {
        path: 'documents/upload',
        component: DocumentUploadComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },
      {
        path: 'documents/view/:id',
        component: DocumentViewComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },
      {
        path: 'documents/view',
        component: DocumentViewComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },
      {
        path: 'documents/reference/:refId',
        component: DocumentReferenceComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },
      {
        path: 'documents/reference',
        component: DocumentReferenceComponent,
        canActivate: [RoleGuard],
        data: { roles: documentRoles }
      },

      // Module 15: Notification
      {
        path: 'notifications/templates',
        component: NotificationTemplateListComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationTemplateRoles }
      },
      {
        path: 'notifications/templates/create',
        component: NotificationTemplateCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationTemplateAdminRoles }
      },
      {
        path: 'notifications/templates/edit/:id',
        component: NotificationTemplateCreateComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationTemplateAdminRoles }
      },
      {
        path: 'notifications/templates/view/:id',
        component: NotificationTemplateViewComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationTemplateRoles }
      },
      {
        path: 'notifications/templates/view',
        component: NotificationTemplateViewComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationTemplateRoles }
      },
      {
        path: 'notifications/send',
        component: NotificationSendComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationSendRoles }
      },
      {
        path: 'notifications/view/:id',
        component: NotificationViewComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationRoles }
      },
      {
        path: 'notifications/view',
        component: NotificationViewComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationRoles }
      },
      {
        path: 'notifications/reference/:refId',
        component: NotificationReferenceComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationRoles }
      },
      {
        path: 'notifications/reference',
        component: NotificationReferenceComponent,
        canActivate: [RoleGuard],
        data: { roles: notificationRoles }
      },

      // Module 16: Report
      {
        path: 'reports/summary',
        component: ReportSummaryComponent,
        canActivate: [RoleGuard],
        data: { roles: reportRoles }
      },
      {
        path: 'reports/policies',
        component: ReportPoliciesComponent,
        canActivate: [RoleGuard],
        data: { roles: reportPolicyRoles }
      },
      {
        path: 'reports/claims',
        component: ReportClaimsComponent,
        canActivate: [RoleGuard],
        data: { roles: reportClaimRoles }
      },
      {
        path: 'reports/premiums',
        component: ReportPremiumsComponent,
        canActivate: [RoleGuard],
        data: { roles: reportPremiumRoles }
      },
      { path: 'reports', redirectTo: 'reports/summary', pathMatch: 'full' },

      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
