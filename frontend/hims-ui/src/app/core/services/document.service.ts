import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentResponse, DocumentStatus, DocumentType } from '../models/document.model';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  constructor(private http: HttpClient) {}

  uploadDocument(file: File, documentType: DocumentType, referenceId?: string): Observable<DocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    if (referenceId) {
      formData.append('referenceId', referenceId);
    }
    return this.http.post<DocumentResponse>('/api/documents', formData);
  }

  getDocumentMetadata(documentId: string): Observable<DocumentResponse> {
    return this.http.get<DocumentResponse>(`/api/documents/${documentId}`);
  }

  downloadDocument(documentId: string): Observable<Blob> {
    return this.http.get(`/api/documents/${documentId}/download`, {
      responseType: 'blob'
    });
  }

  getDocumentsByReferenceId(referenceId: string): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`/api/documents/reference/${referenceId}`);
  }

  updateDocumentStatus(documentId: string, status: DocumentStatus): Observable<DocumentResponse> {
    let params = new HttpParams().set('status', status);
    return this.http.patch<DocumentResponse>(`/api/documents/${documentId}/status`, null, { params });
  }
}
