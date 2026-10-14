import { createContext } from 'react';
import type { TokenClaims } from './token';

export interface AuthValue {
  token: string | null;
  /** Claims of the active token. Null when logged out or when the token has expired. */
  claims: TokenClaims | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (token: string) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthValue | null>(null);
