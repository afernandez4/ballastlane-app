import axios, { type AxiosError, type AxiosInstance } from 'axios';
import type { ProblemDetail } from './types';

/** Error with the backend ProblemDetail already parsed. Status 0 means the network failed. */
export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;
  /** First message per field, for showing next to form inputs. */
  readonly fieldErrors: Record<string, string>;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? 'Error inesperado');
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
    this.fieldErrors = {};
    for (const error of problem.errors ?? []) {
      this.fieldErrors[error.field] ??= error.message;
    }
  }
}

export const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  headers: { Accept: 'application/json' },
});

let currentToken: () => string | null = () => null;
let onUnauthorized: () => void = () => {};

/** Connected by the auth provider: where to read the token and what to do on a rejected token. */
export function configureAuth(getToken: () => string | null, handleUnauthorized: () => void) {
  currentToken = getToken;
  onUnauthorized = handleUnauthorized;
}

api.interceptors.request.use((config) => {
  const token = currentToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetail>) => {
    // A 401 while logged in means the token expired or was rejected: end the session.
    // A 401 from the login form itself must not log anyone out.
    if (error.response?.status === 401 && currentToken()) {
      onUnauthorized();
    }
    return Promise.reject(toApiError(error));
  },
);

function toApiError(error: AxiosError<ProblemDetail>): ApiError {
  if (!error.response) {
    return new ApiError(0, {
      title: 'No connection',
      detail: 'Could not reach the server. Check your connection.',
    });
  }
  return new ApiError(error.response.status, error.response.data ?? {});
}
