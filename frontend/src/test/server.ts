import { setupServer } from 'msw/node';
import { handlers } from './handlers';

/** Shared MSW server for tests. Individual tests can add handlers with server.use(). */
export const server = setupServer(...handlers);
