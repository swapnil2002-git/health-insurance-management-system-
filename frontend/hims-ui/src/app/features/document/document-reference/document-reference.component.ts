import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DocumentResponse } from '../../../core/models/document.model';

@Component({
  selector: 'app-document-reference',
  templateUrl: './document-reference.component.html',
  styleUrls: ['./document-reference.component.scss']
})
export class DocumentReferenceComponent implements OnInit {
  searchForm!: FormGroup;
  documents: DocumentResponse[] = [];
  referenceId: string = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private documentService: DocumentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      referenceId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]]
    });

    const paramId = this.route.snapshot.paramMap.get('refId');
    if (paramId) {
      this.searchForm.patchValue({ referenceId: paramId });
      this.loadDocuments(paramId);
    } else {
      this.route.queryParams.subscribe((params) => {
        if (params['referenceId']) {
          this.searchForm.patchValue({ referenceId: params['referenceId'] });
          this.loadDocuments(params['referenceId']);
        }
      });
    }
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    const id = this.searchForm.value.referenceId.trim();
    this.loadDocuments(id);
  }

  loadDocuments(refId: string): void {
    this.isLoading = true;
    this.referenceId = refId;
    this.documents = [];

    this.documentService.getDocumentsByReferenceId(refId).subscribe({
      next: (list) => {
        this.documents = list || [];
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Failed to retrieve documents for reference ID');
      }
    });
  }

  downloadDocument(doc: DocumentResponse, event: Event): void {
    event.stopPropagation();
    this.documentService.downloadDocument(doc.documentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = doc.fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.notificationService.error('Failed to download document file');
      }
    });
  }

  viewMetadata(docId: string): void {
    this.router.navigate(['/documents/view', docId]);
  }

  uploadNew(): void {
    this.router.navigate(['/documents/upload'], {
      queryParams: { referenceId: this.referenceId }
    });
  }

  formatBytes(bytes?: number): string {
    if (!bytes || bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
}
