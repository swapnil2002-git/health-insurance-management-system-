import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClaimService } from '../../../core/services/claim.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  ClaimResponse,
  ClaimDocumentRequest,
  ClaimServiceRequest,
  ClaimDiagnosisRequest
} from '../../../core/models/claim.model';

@Component({
  selector: 'app-claim-view',
  templateUrl: './claim-view.component.html',
  styleUrls: ['./claim-view.component.scss']
})
export class ClaimViewComponent implements OnInit {
  searchForm!: FormGroup;
  docForm!: FormGroup;
  serviceLineForm!: FormGroup;
  diagForm!: FormGroup;

  claim: ClaimResponse | null = null;
  isLoading = false;
  isAddingDoc = false;
  isAddingLine = false;
  isAddingDiag = false;

  showAddDoc = false;
  showAddLine = false;
  showAddDiag = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private claimService: ClaimService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      query: ['', [Validators.required]]
    });

    this.initForms();

    const idFromParam = this.route.snapshot.paramMap.get('id');
    if (idFromParam) {
      this.searchForm.patchValue({ query: idFromParam });
      this.loadClaim(idFromParam);
    } else {
      this.route.queryParams.subscribe((params) => {
        if (params['claimId']) {
          this.searchForm.patchValue({ query: params['claimId'] });
          this.loadClaim(params['claimId']);
        }
      });
    }
  }

  private initForms(): void {
    const today = new Date().toISOString().split('T')[0];

    this.docForm = this.fb.group({
      documentId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]],
      documentType: ['MEDICAL_BILL', [Validators.required]],
      documentName: ['', [Validators.required]]
    });

    this.serviceLineForm = this.fb.group({
      serviceCode: ['', [Validators.required]],
      serviceDescription: ['', [Validators.required]],
      serviceDate: [today, [Validators.required]],
      unitPrice: [100, [Validators.required, Validators.min(0.01)]],
      quantity: [1, [Validators.required, Validators.min(1)]]
    });

    this.diagForm = this.fb.group({
      diagnosisCode: ['', [Validators.required]],
      description: ['', [Validators.required]],
      primary: [false]
    });
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    const q = this.searchForm.value.query.trim();
    this.loadClaim(q);
  }

  loadClaim(query: string): void {
    this.isLoading = true;
    this.claim = null;

    const isUuid = /^[0-9a-fA-F-]{36}$/.test(query);
    const obs = isUuid
      ? this.claimService.getClaimById(query)
      : this.claimService.getClaimByNumber(query);

    obs.subscribe({
      next: (res) => {
        this.claim = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Claim not found');
      }
    });
  }

  addDocument(): void {
    if (!this.claim || this.docForm.invalid) {
      this.docForm.markAllAsTouched();
      return;
    }

    const payload: ClaimDocumentRequest = {
      documentId: this.docForm.value.documentId.trim(),
      documentType: this.docForm.value.documentType,
      documentName: this.docForm.value.documentName.trim()
    };

    this.isAddingDoc = true;
    this.claimService.addDocumentReference(this.claim.claimId, payload).subscribe({
      next: () => {
        this.isAddingDoc = false;
        this.showAddDoc = false;
        this.docForm.reset({ documentType: 'MEDICAL_BILL' });
        this.notificationService.success('Document reference attached to claim');
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isAddingDoc = false;
        this.notificationService.error(err.error?.message || 'Failed to attach document reference');
      }
    });
  }

  addServiceLine(): void {
    if (!this.claim || this.serviceLineForm.invalid) {
      this.serviceLineForm.markAllAsTouched();
      return;
    }

    const formVal = this.serviceLineForm.value;
    const payload: ClaimServiceRequest = {
      serviceCode: formVal.serviceCode.trim(),
      serviceDescription: formVal.serviceDescription.trim(),
      serviceDate: formVal.serviceDate,
      unitPrice: Number(formVal.unitPrice),
      quantity: Number(formVal.quantity)
    };

    this.isAddingLine = true;
    this.claimService.addServiceLine(this.claim.claimId, payload).subscribe({
      next: () => {
        this.isAddingLine = false;
        this.showAddLine = false;
        this.serviceLineForm.reset({
          serviceDate: new Date().toISOString().split('T')[0],
          unitPrice: 100,
          quantity: 1
        });
        this.notificationService.success('Service line item added');
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isAddingLine = false;
        this.notificationService.error(err.error?.message || 'Failed to add service line item');
      }
    });
  }

  addDiagnosis(): void {
    if (!this.claim || this.diagForm.invalid) {
      this.diagForm.markAllAsTouched();
      return;
    }

    const formVal = this.diagForm.value;
    const payload: ClaimDiagnosisRequest = {
      diagnosisCode: formVal.diagnosisCode.trim(),
      description: formVal.description.trim(),
      primary: !!formVal.primary
    };

    this.isAddingDiag = true;
    this.claimService.addDiagnosis(this.claim.claimId, payload).subscribe({
      next: () => {
        this.isAddingDiag = false;
        this.showAddDiag = false;
        this.diagForm.reset({ primary: false });
        this.notificationService.success('ICD-10 diagnosis attached to claim');
        this.loadClaim(this.claim!.claimId);
      },
      error: (err) => {
        this.isAddingDiag = false;
        this.notificationService.error(err.error?.message || 'Failed to add diagnosis');
      }
    });
  }

  goToValidate(): void {
    if (this.claim) {
      this.router.navigate(['/claims/validate'], { queryParams: { claimId: this.claim.claimId } });
    }
  }

  goToEligibility(): void {
    if (this.claim) {
      this.router.navigate(['/claims/eligibility'], { queryParams: { claimId: this.claim.claimId } });
    }
  }

  goToAdjudicate(): void {
    if (this.claim) {
      this.router.navigate(['/claims/adjudicate'], { queryParams: { claimId: this.claim.claimId } });
    }
  }

  goToSettle(): void {
    if (this.claim) {
      this.router.navigate(['/claims/settle'], { queryParams: { claimId: this.claim.claimId } });
    }
  }

  goToEob(): void {
    if (this.claim) {
      this.router.navigate(['/claims/payment'], { queryParams: { claimId: this.claim.claimId } });
    }
  }
}
