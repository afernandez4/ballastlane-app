import { HttpResponse, http } from 'msw';
import { server } from '../test/server';
import { ApiError, api, configureAuth } from './client';

describe('api client', () => {
  afterEach(() =>
    configureAuth(
      () => null,
      () => {},
    ),
  );

  it('sends the bearer token when one is configured', async () => {
    configureAuth(
      () => 'secret-token',
      () => {},
    );
    let authorization: string | null = null;
    server.use(
      http.get('*/api/v1/auth/me', ({ request }) => {
        authorization = request.headers.get('Authorization');
        return HttpResponse.json({ username: 'ash' });
      }),
    );

    await api.get('/api/v1/auth/me');

    expect(authorization).toBe('Bearer secret-token');
  });

  it('calls the unauthorized handler when a logged-in request gets 401', async () => {
    const onUnauthorized = vi.fn();
    configureAuth(() => 'expired-token', onUnauthorized);
    server.use(
      http.get('*/api/v1/auth/me', () =>
        HttpResponse.json({ title: 'Unauthorized', status: 401 }, { status: 401 }),
      ),
    );

    await expect(api.get('/api/v1/auth/me')).rejects.toBeInstanceOf(ApiError);
    expect(onUnauthorized).toHaveBeenCalledTimes(1);
  });

  it('does not log out on a 401 from anonymous login attempts', async () => {
    const onUnauthorized = vi.fn();
    configureAuth(() => null, onUnauthorized);
    server.use(
      http.post('*/api/v1/auth/login', () =>
        HttpResponse.json({ title: 'Unauthorized', status: 401 }, { status: 401 }),
      ),
    );

    await expect(api.post('/api/v1/auth/login', {})).rejects.toBeInstanceOf(ApiError);
    expect(onUnauthorized).not.toHaveBeenCalled();
  });

  it('turns ProblemDetail validation errors into field messages', async () => {
    server.use(
      http.post('*/api/v1/pokemon', () =>
        HttpResponse.json(
          {
            title: 'Invalid request',
            status: 400,
            errors: [{ field: 'localName', message: 'must not be blank' }],
          },
          { status: 400 },
        ),
      ),
    );

    const error = await api.post('/api/v1/pokemon', {}).catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).status).toBe(400);
    expect((error as ApiError).fieldErrors).toEqual({ localName: 'must not be blank' });
  });

  it('reports a network failure as status 0', async () => {
    server.use(http.get('*/api/v1/pokeapi/pokemon', () => HttpResponse.error()));

    const error = await api.get('/api/v1/pokeapi/pokemon').catch((e: unknown) => e);

    expect((error as ApiError).status).toBe(0);
    expect((error as ApiError).message).toMatch(/connection/i);
  });
});
