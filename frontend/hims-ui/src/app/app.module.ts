import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

// Material Modules
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatBadgeModule } from '@angular/material/badge';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Components: Core Shell
import { AppComponent } from './app.component';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { HeaderComponent } from './shared/components/header/header.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';

// Module 3: User Management Components
import { UserRegisterComponent } from './features/user-management/user-register/user-register.component';
import { UserAuthorizeComponent } from './features/user-management/user-authorize/user-authorize.component';
import { UserRoleComponent } from './features/user-management/user-role/user-role.component';

// Module 4: Customer Components
import { PersonalInfoComponent } from './features/customer/personal-info/personal-info.component';
import { CustomerContextBarComponent } from './features/customer/components/customer-context-bar/customer-context-bar.component';
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

// Material Modules
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

// Interceptors
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';
import { ErrorInterceptor } from './core/interceptors/error.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    DashboardComponent,
    LayoutComponent,
    HeaderComponent,
    SidebarComponent,
    UserRegisterComponent,
    UserAuthorizeComponent,
    UserRoleComponent,
    PersonalInfoComponent,
    CustomerContextBarComponent,
    MemberDetailsComponent,
    BeneficiaryDetailsComponent,
    ContactDetailsComponent,
    AddressDetailsComponent,
    NomineeDetailsComponent,
    ProductListComponent,
    PlanListComponent,
    PlanRuleMappingComponent,
    MasterRulesComponent,
    CreateQuoteComponent,
    CalculateQuoteComponent,
    QuoteDecisionComponent,
    QuoteViewComponent,
    QuoteListComponent,
    RiskAssessComponent,
    RiskCalculateComponent,
    RiskViewComponent,
    UnderwritingCasesComponent,
    UnderwritingApproveComponent,
    UnderwritingRejectComponent,
    UnderwritingReferComponent,
    UnderwritingViewComponent,
    PolicyListComponent,
    PolicyIssueComponent,
    PolicyViewComponent,
    PolicyEndorsementComponent,
    PolicyRenewalComponent,
    PolicyCancelComponent,
    PremiumScheduleComponent,
    PremiumRecalculateComponent,
    PremiumInstallmentComponent,
    PremiumViewComponent,
    PaymentInitiateComponent,
    PaymentConfirmComponent,
    PaymentRefundComponent,
    PaymentViewComponent,
    PaymentListComponent,
    ProviderCreateComponent,
    ProviderAddressComponent,
    ProviderViewComponent,
    ProviderListComponent,
    NetworkCreateComponent,
    NetworkMappingComponent,
    NetworkListComponent,
    ClaimListComponent,
    ClaimCreateComponent,
    ClaimValidateComponent,
    ClaimEligibilityComponent,
    ClaimAdjudicateComponent,
    ClaimSettleComponent,
    ClaimViewComponent,
    ClaimPaymentComponent,
    DocumentUploadComponent,
    DocumentViewComponent,
    DocumentReferenceComponent,
    DocumentListComponent,
    NotificationTemplateListComponent,
    NotificationTemplateCreateComponent,
    NotificationTemplateViewComponent,
    NotificationSendComponent,
    NotificationViewComponent,
    NotificationReferenceComponent,
    ReportSummaryComponent,
    ReportPoliciesComponent,
    ReportClaimsComponent,
    ReportPremiumsComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    AppRoutingModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatToolbarModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatBadgeModule,
    MatProgressBarModule,
    MatSlideToggleModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
