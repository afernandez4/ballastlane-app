import { createContext } from 'react';

export type ToastTone = 'success' | 'error';

export interface ToastValue {
  show: (message: string, tone?: ToastTone) => void;
}

export const ToastContext = createContext<ToastValue | null>(null);
