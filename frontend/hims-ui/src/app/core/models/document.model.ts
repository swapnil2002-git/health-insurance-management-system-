export type DocumentType =
  | 'IDENTITY_DOCUMENT'
  | 'POLICY_DOCUMENT'
  | 'MEDICAL_DOCUMENT'
  | 'CLAIM_DOCUMENT'
  | 'EOB'
  | 'RECEIPT';

export type DocumentStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';

export interface DocumentResponse {
  documentId: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  storageReference: string;
  documentType: DocumentType;
  referenceId?: string;
  status: DocumentStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface DocumentUploadRequest {
  file: File;
  documentType: DocumentType;
  referenceId?: string;
}
