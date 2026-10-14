import { api } from './client';
import type { TokenResponse, UserInfo } from './types';

export interface RegisterInput {
  username: string;
  email: string;
  password: string;
}

export async function register(input: RegisterInput): Promise<UserInfo> {
  const { data } = await api.post<UserInfo>('/api/v1/auth/register', input);
  return data;
}

export async function login(username: string, password: string): Promise<TokenResponse> {
  const { data } = await api.post<TokenResponse>('/api/v1/auth/login', { username, password });
  return data;
}

export async function fetchMe(): Promise<UserInfo> {
  const { data } = await api.get<UserInfo>('/api/v1/auth/me');
  return data;
}
