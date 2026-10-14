import type { ReactNode } from 'react';
import { useAuth } from './useAuth';

/**
 * Hides admin-only controls. This is a UX guard only: the backend enforces ADMIN on every
 * destructive call.
 */
export function AdminOnly({ children }: { children: ReactNode }) {
  const { isAdmin } = useAuth();
  return isAdmin ? <>{children}</> : null;
}
