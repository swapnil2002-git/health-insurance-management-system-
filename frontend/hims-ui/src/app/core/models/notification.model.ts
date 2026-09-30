export type NotificationChannel = 'EMAIL' | 'SMS' | 'IN_APP';

export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED';

export type TemplateStatus = 'ACTIVE' | 'INACTIVE';

export interface NotificationTemplateRequest {
  eventType: string;
  channel: NotificationChannel;
  subjectTemplate?: string;
  bodyTemplate: string;
  status?: TemplateStatus;
}

export interface NotificationTemplateResponse {
  templateId: string;
  eventType: string;
  channel: NotificationChannel;
  subjectTemplate?: string;
  bodyTemplate: string;
  status: TemplateStatus;
  createdAt: string;
  updatedAt?: string;
}

export interface NotificationRequest {
  recipient: string;
  channel: NotificationChannel;
  eventType?: string;
  subject?: string;
  content?: string;
  referenceId?: string;
  eventId?: string;
  templateData?: { [key: string]: any };
}

export interface NotificationResponse {
  notificationId: string;
  recipient: string;
  channel: NotificationChannel;
  eventType?: string;
  subject?: string;
  content?: string;
  status: NotificationStatus;
  referenceId?: string;
  eventId?: string;
  errorMessage?: string;
  sentAt?: string;
  createdAt: string;
}
