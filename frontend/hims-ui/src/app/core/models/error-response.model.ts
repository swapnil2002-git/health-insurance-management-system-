export interface ErrorResponse {
  timestamp: string;
  status: number;
  errorCode: string;
  message: string;
  path: string;
  correlationId?: string;
  validationErrors?: Record<string, string>;
}
