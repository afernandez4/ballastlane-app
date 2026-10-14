import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { HttpResponse, http } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { server } from '../test/server';
import { renderWithProviders } from '../test/renderWithProviders';
import { SESSION_KEY } from '../auth/AuthProvider';
import { EditPokemonPage } from './EditPokemonPage';

const pokemon = {
  id: 1,
  pokeApiId: 1,
  name: 'bulbasaur',
  spriteUrl: null,
  category: 'Seed Pokémon',
  height: 7,
  weight: 69,
  types: [{ name: 'grass', slot: 1 }],
  abilities: [],
  localName: 'Semillita',
  region: 'Kanto',
  tags: ['starter', 'grass'],
  version: 2,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

function tokenFor(role: 'USER' | 'ADMIN'): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'HS384' })}.${encode({ sub: 'ash', role, exp: 4102444800 })}.sig`;
}

function renderEdit() {
  sessionStorage.setItem(SESSION_KEY, tokenFor('USER'));
  server.use(
    http.get('*/api/v1/pokemon/1', () => HttpResponse.json(pokemon, { headers: { ETag: '"2"' } })),
  );
  return renderWithProviders(
    <Routes>
      <Route path="/my-pokemon/:id/edit" element={<EditPokemonPage />} />
    </Routes>,
    { route: '/my-pokemon/1/edit' },
  );
}

describe('EditPokemonPage', () => {
  it('loads the stored own fields into the form', async () => {
    renderEdit();

    expect(await screen.findByDisplayValue('Semillita')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Kanto')).toBeInTheDocument();
    expect(screen.getByDisplayValue('starter, grass')).toBeInTheDocument();
  });

  it('blocks invalid tags before sending the request', async () => {
    const requests = vi.fn();
    server.events.on('request:start', ({ request }) => {
      if (request.method === 'PUT') requests();
    });
    renderEdit();
    const tags = await screen.findByLabelText('Tags');

    await userEvent.clear(tags);
    await userEvent.type(tags, 'a, b, c, d, e, f, g, h, i, j, k');
    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('At most 10 tags')).toBeInTheDocument();
    expect(requests).not.toHaveBeenCalled();
    server.events.removeAllListeners();
  });

  it('maps server validation errors onto the matching field', async () => {
    renderEdit();
    await screen.findByDisplayValue('Semillita');
    server.use(
      http.put('*/api/v1/pokemon/1', () =>
        HttpResponse.json(
          {
            title: 'Invalid request',
            status: 400,
            errors: [{ field: 'region', message: 'must be at most 100 characters' }],
          },
          { status: 400 },
        ),
      ),
    );

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('must be at most 100 characters')).toBeInTheDocument();
    expect(screen.getByLabelText('Region')).toHaveAttribute('aria-invalid', 'true');
  });

  it('shows a reload message and blocks saving on a version conflict (409)', async () => {
    renderEdit();
    await screen.findByDisplayValue('Semillita');
    server.use(
      http.put('*/api/v1/pokemon/1', () =>
        HttpResponse.json({ title: 'Conflict', status: 409 }, { status: 409 }),
      ),
    );

    await userEvent.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Someone else changed this Pokémon');
    await waitFor(() => expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled());
  });
});
