export interface NavItem {
  title: string;
  icon: string;
  route?: string;
  roles?: string[];
  submenus?: NavSubItem[];
  expanded?: boolean;
}

export interface NavSubItem {
  title: string;
  route: string;
  icon?: string;
  roles?: string[];
}

export const NAVIGATION_ITEMS: NavItem[] = [
  {
    title: 'Dashboard',
    icon: 'dashboard',
    route: '/dashboard',
    roles: ['CUSTOMER', 'AGENT', 'UNDERWRITER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'HEALTHCARE_PROVIDER', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
  },
  {
    title: 'User Management',
    icon: 'manage_accounts',
    roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Register User', route: '/users/register' },
      { title: 'Authorize User', route: '/users/authorize' },
      { title: 'Change Role', route: '/users/role' }
    ]
  },
  {
    title: 'Customer',
    icon: 'people',
    roles: ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Personal Information', route: '/customers/info' },
      { title: 'Member Details', route: '/customers/members' },
      { title: 'Beneficiary Details', route: '/customers/beneficiaries' },
      { title: 'Contact Details', route: '/customers/contacts' },
      { title: 'Address Details', route: '/customers/addresses' },
      { title: 'Nominee Details', route: '/customers/nominees' }
    ]
  },
  {
    title: 'Product & Plan',
    icon: 'category',
    roles: ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Products', route: '/products' },
      { title: 'Plans', route: '/plans' },
      { title: 'Plan-Rule Mapping', route: '/plans/rule-mapping' },
      { title: 'View Master Rules', route: '/plans/rules' }
    ]
  },
  {
    title: 'Quotation',
    icon: 'request_quote',
    roles: ['CUSTOMER', 'AGENT', 'UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Create Quotation', route: '/quotes/create' },
      { title: 'Calculate Quotation', route: '/quotes/calculate' },
      { title: 'Accept / Reject Quote', route: '/quotes/decision' },
      { title: 'View Quotation', route: '/quotes/view' },
      { title: 'View All Quotations', route: '/quotes' }
    ]
  },
  {
    title: 'Risk Assessment',
    icon: 'speed',
    roles: ['UNDERWRITER', 'AGENT', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Assess Risk', route: '/risk/assess' },
      { title: 'Calculate Risk', route: '/risk/calculate' },
      { title: 'View Risk Assessment', route: '/risk/view' }
    ]
  },
  {
    title: 'Underwriting',
    icon: 'rule',
    roles: ['UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Underwriting Cases', route: '/underwriting/cases' },
      { title: 'View Case', route: '/underwriting/view' }
    ]
  },
  {
    title: 'Policy',
    icon: 'policy',
    roles: ['CUSTOMER', 'AGENT', 'POLICY_ADMINISTRATOR', 'UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Issue Policy', route: '/policies/issue' },
      { title: 'View Policy by ID', route: '/policies/view' },
      { title: 'View All Policies', route: '/policies' },
      { title: 'Endorse Policy', route: '/policies/endorse' },
      { title: 'Renewal Quote', route: '/policies/renew' },
      { title: 'Cancel Policy', route: '/policies/cancel' }
    ]
  },
  {
    title: 'Premium',
    icon: 'account_balance',
    roles: ['CUSTOMER', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Premium Schedule', route: '/premium/schedules' },
      { title: 'Premium Recalculation', route: '/premium/recalculate' },
      { title: 'Installment Payment', route: '/premium/installments' },
      { title: 'View Premium', route: '/premium/view' }
    ]
  },
  {
    title: 'Payment',
    icon: 'payments',
    roles: ['CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Pay Installment', route: '/payments/pay' },
      { title: 'Confirm Payment', route: '/payments/confirm' },
      { title: 'Refund Payment', route: '/payments/refund' },
      { title: 'View Payment by ID', route: '/payments/view' },
      { title: 'View All Payments', route: '/payments' }
    ]
  },
  {
    title: 'Provider',
    icon: 'local_hospital',
    roles: ['HEALTHCARE_PROVIDER', 'POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Create Provider', route: '/providers/create' },
      { title: 'Add Address', route: '/providers/address' },
      { title: 'View Provider by ID', route: '/providers/view' },
      { title: 'View All Providers', route: '/providers' },
      { title: 'Create Network', route: '/providers/networks/create' },
      { title: 'Provider-Network Mapping', route: '/providers/networks/mapping' },
      { title: 'View All Networks', route: '/providers/networks' }
    ]
  },
  {
    title: 'Claim',
    icon: 'assignment_turned_in',
    roles: ['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'FINANCE_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Create New Claim', route: '/claims/create' },
      { title: 'Validate Claim', route: '/claims/validate' },
      { title: 'Verify Eligibility', route: '/claims/eligibility' },
      { title: 'Adjudicate Claim', route: '/claims/adjudicate' },
      { title: 'Settle Claim', route: '/claims/settle' },
      { title: 'View Claim by ID', route: '/claims/view' },
      { title: 'View Claim Payment / EOB', route: '/claims/payment' }
    ]
  },
  {
    title: 'Document',
    icon: 'folder_open',
    roles: ['CUSTOMER', 'AGENT', 'HEALTHCARE_PROVIDER', 'CLAIMS_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'Document Directory', route: '/documents' },
      { title: 'Upload Document', route: '/documents/upload' },
      { title: 'View Document by ID', route: '/documents/view' },
      { title: 'Search by Reference ID', route: '/documents/reference' }
    ]
  },
  {
    title: 'Notification',
    icon: 'notifications',
    roles: ['POLICY_ADMINISTRATOR', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN'],
    submenus: [
      { title: 'All Templates', route: '/notifications/templates' },
      { title: 'Create Template', route: '/notifications/templates/create' },
      { title: 'View Template by ID', route: '/notifications/templates/view' },
      { title: 'Dispatch Notification', route: '/notifications/send' },
      { title: 'View Notification by ID', route: '/notifications/view' },
      { title: 'View by Reference ID', route: '/notifications/reference' }
    ]
  },
  {
    title: 'Report',
    icon: 'bar_chart',
    roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN', 'POLICY_ADMINISTRATOR', 'FINANCE_OFFICER', 'CLAIMS_OFFICER', 'UNDERWRITER'],
    submenus: [
      { title: 'Dashboard Summary', route: '/reports/summary' },
      { title: 'Policy Report', route: '/reports/policies' },
      { title: 'Claim Report', route: '/reports/claims' },
      { title: 'Premium Report', route: '/reports/premiums' }
    ]
  }
];
