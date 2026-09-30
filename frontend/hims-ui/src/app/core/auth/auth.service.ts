import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse, RegisterRequest } from '../models/user.model';
import { TokenStorageService } from './token-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = '/api/auth';
  private currentUserSubject = new BehaviorSubject<{ username: string; role: string } | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
    private router: Router
  ) {
    if (this.tokenStorage.isLoggedIn()) {
      const username = this.tokenStorage.getUsername() || '';
      const role = this.tokenStorage.getRole() || '';
      this.currentUserSubject.next({ username, role });
    }
  }

  public login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap((res) => {
        if (res && res.token) {
          this.tokenStorage.saveSession(res.token, res.username, res.role);
          this.currentUserSubject.next({ username: res.username, role: res.role });
        }
      })
    );
  }

  public register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.baseUrl}/register`, request, { responseType: 'text' });
  }

  public logout(): void {
    this.tokenStorage.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  public isAuthenticated(): boolean {
    return this.tokenStorage.isLoggedIn();
  }

  public getRole(): string | null {
    return this.tokenStorage.getRole();
  }

  public getUsername(): string | null {
    return this.tokenStorage.getUsername();
  }

  public hasRole(allowedRoles: string[]): boolean {
    const currentRole = this.getRole();
    if (!currentRole) return false;
    return allowedRoles.includes(currentRole) || allowedRoles.includes('ADMIN') && currentRole === 'SYSTEM_ADMINISTRATOR';
  }
}
