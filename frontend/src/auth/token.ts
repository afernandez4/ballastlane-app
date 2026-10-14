export interface TokenClaims {
  sub: string;
  role: 'USER' | 'ADMIN';
  /** Expiry as seconds since epoch, the JWT convention. */
  exp: number;
}

/**
 * Reads the payload of a JWT. The signature is NOT checked here: the backend is the only party that
 * can verify it. This is only used to show the user name and to hide admin-only buttons.
 */
export function decodeToken(token: string): TokenClaims | null {
  const parts = token.split('.');
  if (parts.length !== 3) {
    return null;
  }
  try {
    const json = atob(parts[1]!.replace(/-/g, '+').replace(/_/g, '/'));
    const payload = JSON.parse(json) as Partial<TokenClaims>;
    if (
      typeof payload.sub !== 'string' ||
      (payload.role !== 'USER' && payload.role !== 'ADMIN') ||
      typeof payload.exp !== 'number'
    ) {
      return null;
    }
    return { sub: payload.sub, role: payload.role, exp: payload.exp };
  } catch {
    return null;
  }
}

export function isExpired(claims: TokenClaims, nowMs: number = Date.now()): boolean {
  return claims.exp * 1000 <= nowMs;
}
