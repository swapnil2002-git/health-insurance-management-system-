import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private static readonly TOKEN_KEY = 'hims_auth_token';
  private static readonly USERNAME_KEY = 'hims_auth_username';
  private static readonly ROLE_KEY = 'hims_auth_role';

  public saveSession(token: string, username: string, role: string): void {
    sessionStorage.removeItem(TokenStorageService.TOKEN_KEY);
    sessionStorage.removeItem(TokenStorageService.USERNAME_KEY);
    sessionStorage.removeItem(TokenStorageService.ROLE_KEY);

    sessionStorage.setItem(TokenStorageService.TOKEN_KEY, token);
    sessionStorage.setItem(TokenStorageService.USERNAME_KEY, username);
    sessionStorage.setItem(TokenStorageService.ROLE_KEY, role);
  }

  public getToken(): string | null {
    return sessionStorage.getItem(TokenStorageService.TOKEN_KEY);
  }

  public getUsername(): string | null {
    return sessionStorage.getItem(TokenStorageService.USERNAME_KEY);
  }

  public getRole(): string | null {
    return sessionStorage.getItem(TokenStorageService.ROLE_KEY);
  }

  public clear(): void {
    sessionStorage.clear();
  }

  public isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
