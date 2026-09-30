import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DocumentResponse, DocumentStatus } from '../../../core/models/document.model';

@Component({
  selector: 'app-document-view',
  templateUrl: './document-view.component.html',
  styleUrls: ['./document-view.component.scss']
})
export class DocumentViewComponent implements OnInit {
  searchForm!: FormGroup;
  document: DocumentResponse | null = null;
  isLoading = false;
  isDownloading = false;
  isUpdatingStatus = false;

  statusOptions: DocumentStatus[] = ['ACTIVE', 'INACTIVE', 'ARCHIVED'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private documentService: DocumentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      documentId: ['', [Validators.required, Validators.pattern(/^[0-9a-fA-F-]{36}$/)]]
    });

    const idFromParam = this.route.snapshot.paramMap.get('id');
    if (idFromParam) {
      this.searchForm.patchValue({ documentId: idFromParam });
      this.loadDocument(idFromParam);
    } else {
      this.route.queryParams.subscribe((params) => {
        if (params['documentId']) {
          this.searchForm.patchValue({ documentId: params['documentId'] });
          this.loadDocument(params['documentId']);
        }
      });
    }
  }

  onSearch(): void {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }
    const id = this.searchForm.value.documentId.trim();
    this.loadDocument(id);
  }

  loadDocument(documentId: string): void {
    this.isLoading = true;
    this.document = null;

    this.documentService.getDocumentMetadata(documentId).subscribe({
      next: (res) => {
        this.document = res;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.notificationService.error(err.error?.message || 'Document not found');
      }
    });
  }

  downloadFile(): void {
    if (!this.document) return;

    this.isDownloading = true;
    this.documentService.downloadDocument(this.document.documentId).subscribe({
      next: (blob) => {
        this.isDownloading = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.document!.fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.isDownloading = false;
        this.notificationService.error('Failed to download physical document file');
      }
    });
  }

  updateStatus(newStatus: DocumentStatus): void {
    if (!this.document) return;

    this.isUpdatingStatus = true;
    this.documentService.updateDocumentStatus(this.document.documentId, newStatus).subscribe({
      next: (updated) => {
        this.isUpdatingStatus = false;
        this.document = updated;
        this.notificationService.success(`Document status updated to ${newStatus}`);
      },
      error: (err) => {
        this.isUpdatingStatus = false;
        this.notificationService.error(err.error?.message || 'Failed to update document status');
      }
    });
  }

  formatBytes(bytes?: number): string {
    if (!bytes || bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  viewRelated(): void {
    if (this.document?.referenceId) {
      this.router.navigate(['/documents/reference', this.document.referenceId]);
    }
  }
}
