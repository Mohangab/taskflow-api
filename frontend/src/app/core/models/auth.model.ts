export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export type Role = 'USER' | 'ADMIN';

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  email: string;
  role: Role;
}

export interface AuthUser {
  userId: number;
  email: string;
  role: Role;
  accessToken: string;
}
