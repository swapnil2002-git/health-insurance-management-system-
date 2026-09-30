import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegisterRequest, UserSummaryResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private readonly baseUrl = '/api/auth';

  constructor(private http: HttpClient) {}

  /**
   * Registers a new user account with a designated role.
   * Customer accounts are activated immediately; staff accounts require admin approval.
   */
  public registerUser(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, request, { responseType: 'text' });
  }

  /**
   * Fetches all user registrations currently in PENDING state awaiting admin approval.
   */
  public getPendingUsers(): Observable<UserSummaryResponse[]> {
    return this.http.get<UserSummaryResponse[]>(`${this.baseUrl}/users/pending`);
  }

  /**
   * Approves a pending user account, activating it for login.
   */
  public approveUser(username: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/users/${encodeURIComponent(username)}/approve`, {});
  }

  /**
   * Rejects a pending user registration.
   */
  public rejectUser(username: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/users/${encodeURIComponent(username)}/reject`, {});
  }

  /**
   * Assigns or updates the role of an existing user.
   */
  public assignRole(username: string, role: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(
      `${this.baseUrl}/users/${encodeURIComponent(username)}/role?role=${encodeURIComponent(role)}`,
      {}
    );
  }
}
