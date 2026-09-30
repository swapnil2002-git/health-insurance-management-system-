import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  NotificationChannel,
  NotificationRequest,
  NotificationResponse,
  NotificationTemplateRequest,
  NotificationTemplateResponse
} from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationApiService {
  private readonly templateBaseUrl = '/api/notification-templates';
  private readonly notificationBaseUrl = '/api/notifications';

  constructor(private http: HttpClient) {}

  // ================= Notification Templates =================

  createTemplate(request: NotificationTemplateRequest): Observable<NotificationTemplateResponse> {
    return this.http.post<NotificationTemplateResponse>(this.templateBaseUrl, request);
  }

  updateTemplate(id: string, request: NotificationTemplateRequest): Observable<NotificationTemplateResponse> {
    return this.http.put<NotificationTemplateResponse>(`${this.templateBaseUrl}/${id}`, request);
  }

  getTemplateById(id: string): Observable<NotificationTemplateResponse> {
    return this.http.get<NotificationTemplateResponse>(`${this.templateBaseUrl}/${id}`);
  }

  getAllTemplates(eventType?: string): Observable<NotificationTemplateResponse[]> {
    let params = new HttpParams();
    if (eventType && eventType.trim()) {
      params = params.set('eventType', eventType.trim());
    }
    return this.http.get<NotificationTemplateResponse[]>(this.templateBaseUrl, { params });
  }

  getTemplateByEventAndChannel(eventType: string, channel: NotificationChannel): Observable<NotificationTemplateResponse> {
    return this.http.get<NotificationTemplateResponse>(`${this.templateBaseUrl}/event/${eventType}/channel/${channel}`);
  }

  deleteTemplate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.templateBaseUrl}/${id}`);
  }

  // ================= Notifications =================

  sendNotification(request: NotificationRequest): Observable<NotificationResponse> {
    return this.http.post<NotificationResponse>(this.notificationBaseUrl, request);
  }

  getNotificationById(id: string): Observable<NotificationResponse> {
    return this.http.get<NotificationResponse>(`${this.notificationBaseUrl}/${id}`);
  }

  getNotificationsByRecipient(recipient: string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.notificationBaseUrl}/recipient/${recipient}`);
  }

  getNotificationsByReferenceId(referenceId: string): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.notificationBaseUrl}/reference/${referenceId}`);
  }
}
