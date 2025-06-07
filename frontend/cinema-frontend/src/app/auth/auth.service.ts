import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, catchError, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

function encodeBasicAuth(username: string, password: string): string {
  const raw = `${username}:${password}`;
  const utf8 = new TextEncoder().encode(raw);
  const base64 = window.btoa(String.fromCharCode(...utf8));
  return `Basic ${base64}`;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = 'http://localhost:8081/psiw/api/v1/auth';
  private _loggedIn = new BehaviorSubject<boolean>(this.hasTokens());
  readonly loggedIn$ = this._loggedIn.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      Authorization: encodeBasicAuth(username, password),
    });

    return this.http
      .post<LoginResponse>(`${this.baseUrl}/login`, {}, { headers })
      .pipe(
        tap((res) => {
          localStorage.setItem('accessToken', res.accessToken);
          localStorage.setItem('refreshToken', res.refreshToken);
          this._loggedIn.next(true);
        })
      );
  }

  refresh(): Observable<LoginResponse> {
    const token = localStorage.getItem('refreshToken');
    if (!token) throw new Error('No refresh token available');

    const headers = new HttpHeaders({
      'X-Refresh-Token': token,
    });

    console.log('Sending refresh token:', token);

    return this.http
      .post<LoginResponse>(`${this.baseUrl}/refresh`, {}, { headers })
      .pipe(
        tap({
          next: (res) => {
            console.log('Token refreshed successfully');
            localStorage.setItem('accessToken', res.accessToken);
            this._loggedIn.next(true);
          },
        }),
        catchError((err) => {
          console.error('Refresh error', err);
          this.logout();
          throw err;
        })
      );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this._loggedIn.next(false);
    this.router.navigate(['/repertoire']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  isStaff(): boolean {
    return this.hasTokens();
  }

  private hasTokens(): boolean {
    return (
      !!localStorage.getItem('accessToken') &&
      !!localStorage.getItem('refreshToken')
    );
  }
}
