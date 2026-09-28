import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';

const STORAGE_KEY = 'taskflow_auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSignal = signal<AuthUser | null>(this.readStoredUser());

  readonly user = this.userSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.userSignal()?.accessToken);
  readonly email = computed(() => this.userSignal()?.email ?? null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/register`, request)
      .pipe(tap((res) => this.persist(res)));
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/api/auth/login`, request)
      .pipe(tap((res) => this.persist(res)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.userSignal.set(null);
    void this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.userSignal()?.accessToken ?? null;
  }

  private persist(res: AuthResponse): void {
    const user: AuthUser = {
      userId: res.userId,
      email: res.email,
      role: res.role,
      accessToken: res.accessToken,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    this.userSignal.set(user);
  }

  private readStoredUser(): AuthUser | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return null;
      }
      const parsed = JSON.parse(raw) as AuthUser;
      if (!parsed?.accessToken) {
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }
}
