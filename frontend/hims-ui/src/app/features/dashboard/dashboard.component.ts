import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ReportService } from '../../core/services/report.service';
import { DashboardSummary } from '../../core/models/report.model';

interface KpiCard {
  title: string;
  value: string;
  subtitle: string;
  icon: string;
  color: string;
}

interface QuickAction {
  title: string;
  description: string;
  route: string;
  icon: string;
  roles: string[];
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  username = '';
  role = '';
  userRoleLabel = '';
  isLoading = false;
  summary: DashboardSummary | null = null;
  lastUpdated: Date | null = null;

  kpiCards: KpiCard[] = [
    {
      title: 'Active Policies',
      value: '-',
      subtitle: 'Loading live policies...',
      icon: 'verified',
      color: '#0d47a1'
    },
    {
      title: 'Claims Processed',
      value: '-',
      subtitle: 'Loading live claims...',
      icon: 'fact_check',
      color: '#2e7d32'
    },
    {
      title: 'Premium Volume',
      value: '-',
      subtitle: 'Loading live billing...',
      icon: 'account_balance_wallet',
      color: '#00838f'
    },
    {
      title: 'Settled Benefit Payout',
      value: '-',
      subtitle: 'Loading live settlements...',
      icon: 'payments',
      color: '#e65100'
    }
  ];

  quickActions: QuickAction[] = [
    {
      title: 'Authorize Users',
      description: 'Review and approve pending staff registrations',
      route: '/users/authorize',
      icon: 'manage_accounts',
      roles: ['SYSTEM_ADMINISTRATOR', 'ADMIN']
    },
    {
      title: 'Underwriting Queue',
      description: 'Approve, reject, or refer pending risk assessments',
      route: '/underwriting/cases',
      icon: 'rule',
      roles: ['UNDERWRITER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
    },
    {
      title: 'Submit Healthcare Claim',
      description: 'File an insured claim against active policy',
      route: '/claims/create',
      icon: 'add_circle',
      roles: ['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
    },
    {
      title: 'Adjudicate Claims',
      description: 'Execute medical rule adjudication and deductibles',
      route: '/claims/adjudicate',
      icon: 'gavel',
      roles: ['CLAIMS_OFFICER', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
    },
    {
      title: 'Issue & Manage Policies',
      description: 'Issue drafted policies and transition to billing',
      route: '/policies/issue',
      icon: 'policy',
      roles: ['POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
    },
    {
      title: 'Record Premium Payment',
      description: 'Process installment payment or settlement',
      route: '/payments/pay',
      icon: 'credit_card',
      roles: ['CUSTOMER', 'AGENT', 'FINANCE_OFFICER', 'POLICY_ADMINISTRATOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN']
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUsername() || 'Staff Member';
    this.role = this.authService.getRole() || 'CUSTOMER';
    this.userRoleLabel = this.role.replace(/_/g, ' ');
    this.loadDashboardMetrics();
  }

  loadDashboardMetrics(): void {
    this.isLoading = true;
    this.reportService.getDashboardSummary().subscribe({
      next: (data) => {
        this.isLoading = false;
        this.summary = data;
        this.lastUpdated = data.generatedAt ? new Date(data.generatedAt) : new Date();
        this.updateKpis(data);
      },
      error: () => {
        this.isLoading = false;
        // Keep cards with fallback notice if unavailable
        if (!this.summary) {
          this.kpiCards[0].subtitle = 'Live feed unavailable';
          this.kpiCards[1].subtitle = 'Live feed unavailable';
          this.kpiCards[2].subtitle = 'Live feed unavailable';
          this.kpiCards[3].subtitle = 'Live feed unavailable';
        }
      }
    });
  }

  private updateKpis(s: DashboardSummary): void {
    const formattedPremium = new Intl.NumberFormat('en-IN', {
      maximumFractionDigits: 2
    }).format(s.totalPremiumAmount || 0);

    const formattedApproved = new Intl.NumberFormat('en-IN', {
      maximumFractionDigits: 2
    }).format(s.totalApprovedAmount || 0);

    const formattedClaimed = new Intl.NumberFormat('en-IN', {
      maximumFractionDigits: 2
    }).format(s.totalClaimedAmount || 0);

    this.kpiCards = [
      {
        title: 'Active Policies',
        value: (s.totalPoliciesActive ?? 0).toLocaleString(),
        subtitle: `${s.totalPoliciesIssued ?? 0} total policies issued`,
        icon: 'verified',
        color: '#0d47a1'
      },
      {
        title: 'Claims Processed',
        value: (s.totalClaimsSubmitted ?? 0).toLocaleString(),
        subtitle: `${s.totalClaimsApproved ?? 0} approved (${s.claimApprovalRate ?? 0}% rate)`,
        icon: 'fact_check',
        color: '#2e7d32'
      },
      {
        title: 'Premium Volume',
        value: `₹ ${formattedPremium}`,
        subtitle: `${s.totalPaymentsCollected ?? 0} collections received`,
        icon: 'account_balance_wallet',
        color: '#00838f'
      },
      {
        title: 'Settled Benefit Payout',
        value: `₹ ${formattedApproved}`,
        subtitle: `${s.totalClaimsSettled ?? 0} settled (₹ ${formattedClaimed} claimed)`,
        icon: 'payments',
        color: '#e65100'
      }
    ];
  }

  isActionAllowed(roles: string[]): boolean {
    return this.authService.hasRole(roles);
  }

  navigate(route: string): void {
    this.router.navigate([route]);
  }
}
