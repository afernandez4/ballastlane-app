import { http, HttpResponse } from 'msw';

/** Default API handlers. Tests override them with server.use(...) when they need other data. */
export const handlers = [
  http.get('*/api/v1/pokemon', () =>
    HttpResponse.json({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 }),
  ),
];
