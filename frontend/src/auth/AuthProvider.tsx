import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { configureAuth } from '../api/client';
import { AuthContext, type AuthValue } from './authContext';
import { decodeToken, isExpired } from './token';

/** Session token lives in sessionStorage (per tab) and in memory while the app runs. */
export const SESSION_KEY = 'pokedex.session';

function readStoredToken(): string | null {
  try {
    const token = sessionStorage.getItem(SESSION_KEY);
    const claims = token ? decodeToken(token) : null;
    return claims && !isExpired(claims) ? token : null;
  } catch {
    return null; // storage blocked (private mode, policies): start logged out
  }
}

function writeStoredToken(token: string | null) {
  try {
    if (token) {
      sessionStorage.setItem(SESSION_KEY, token);
    } else {
      sessionStorage.removeItem(SESSION_KEY);
    }
  } catch {
    // Storage unavailable: the session still works until the page is reloaded.
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [token, setToken] = useState<string | null>(readStoredToken);
  const tokenRef = useRef(token);

  useEffect(() => {
    tokenRef.current = token;
  }, [token]);

  const login = useCallback((next: string) => {
    setToken(next);
    writeStoredToken(next);
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    writeStoredToken(null);
  }, []);

  // A rejected token ends the session and sends the user to the login page.
  const handleUnauthorized = useCallback(() => {
    logout();
    navigate('/login', { replace: true });
  }, [logout, navigate]);

  useEffect(() => {
    configureAuth(() => tokenRef.current, handleUnauthorized);
  }, [handleUnauthorized]);

  const value = useMemo<AuthValue>(() => {
    const claims = token ? decodeToken(token) : null;
    const active = claims && !isExpired(claims) ? claims : null;
    return {
      token: active ? token : null,
      claims: active,
      isAuthenticated: active !== null,
      isAdmin: active?.role === 'ADMIN',
      login,
      logout,
    };
  }, [token, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
