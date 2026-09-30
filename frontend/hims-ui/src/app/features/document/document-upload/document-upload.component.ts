import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentService } from '../../../core/services/document.service';
import { NotificationService } from '../../../core/services/notification.service';
import { DocumentResponse, DocumentType } from '../../../core/models/document.model';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.scss']
})
export class DocumentUploadComponent implements OnInit {
  uploadForm!: FormGroup;
  selectedFile: File | null = null;
  uploadedDocument: DocumentResponse | null = null;
  isUploading = false;
  isDragging = false;

  documentTypeOptions: { value: DocumentType; label: string; icon: string }[] = [
    { value: 'IDENTITY_DOCUMENT', label: 'Identity Document (Passport, National ID)', icon: 'badge' },
    { value: 'POLICY_DOCUMENT', label: 'Policy Schedule / Certificate', icon: 'policy' },
    { value: 'MEDICAL_DOCUMENT', label: 'Medical Records & Clinical Chart', icon: 'medical_services' },
    { value: 'CLAIM_DOCUMENT', label: 'Hospital Bill / Discharge Summary', icon: 'assignment' },
    { value: 'EOB', label: 'Explanation of Benefits Statement', icon: 'receipt_long' },
    { value: 'RECEIPT', label: 'Payment / Settlement Receipt', icon: 'receipt' }
  ];

  readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  readonly ALLOWED_TYPES = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private documentService: DocumentService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.uploadForm = this.fb.group({
      documentType: ['MEDICAL_DOCUMENT' as DocumentType, [Validators.required]],
      referenceId: ['', [Validators.pattern(/^[0-9a-fA-F-]{36}$/)]]
    });

    this.route.queryParams.subscribe((params) => {
      if (params['referenceId']) {
        this.uploadForm.patchValue({ referenceId: params['referenceId'] });
      }
      if (params['type']) {
        this.uploadForm.patchValue({ documentType: params['type'] });
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.validateAndSetFile(input.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;

    if (event.dataTransfer && event.dataTransfer.files.length > 0) {
      this.validateAndSetFile(event.dataTransfer.files[0]);
    }
  }

  validateAndSetFile(file: File): void {
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      this.notificationService.error('Invalid file format. Only PDF, JPEG, and PNG files are allowed.');
      return;
    }

    if (file.size > this.MAX_FILE_SIZE) {
      this.notificationService.error('File size exceeds the 10MB limit.');
      return;
    }

    this.selectedFile = file;
    this.uploadedDocument = null;
  }

  removeFile(): void {
    this.selectedFile = null;
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      this.notificationService.error('Please select a file to upload.');
      return;
    }

    if (this.uploadForm.invalid) {
      this.uploadForm.markAllAsTouched();
      return;
    }

    this.isUploading = true;
    const docType = this.uploadForm.value.documentType;
    const refId = this.uploadForm.value.referenceId ? this.uploadForm.value.referenceId.trim() : undefined;

    this.documentService.uploadDocument(this.selectedFile, docType, refId).subscribe({
      next: (res) => {
        this.isUploading = false;
        this.uploadedDocument = res;
        this.selectedFile = null;
        this.notificationService.success(`Document "${res.fileName}" uploaded successfully`);
      },
      error: (err) => {
        this.isUploading = false;
        this.notificationService.error(err.error?.message || 'Failed to upload document');
      }
    });
  }

  downloadUploaded(): void {
    if (!this.uploadedDocument) return;
    this.documentService.downloadDocument(this.uploadedDocument.documentId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.uploadedDocument!.fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.notificationService.error('Failed to download document file');
      }
    });
  }

  viewMetadata(): void {
    if (this.uploadedDocument) {
      this.router.navigate(['/documents/view', this.uploadedDocument.documentId]);
    }
  }

  resetUpload(): void {
    this.uploadedDocument = null;
    this.selectedFile = null;
  }
}
