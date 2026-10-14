import { screen } from '@testing-library/react';
import { Route, Routes } from 'react-router-dom';
import { renderWithProviders } from '../test/renderWithProviders';
import { ProtectedRoute } from './ProtectedRoute';
import { SESSION_KEY } from './AuthProvider';

/** Unsigned token with a far-future expiry: the client only reads the payload. */
function tokenFor(role: 'USER' | 'ADMIN', exp = 4102444800): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'HS384' })}.${encode({ sub: 'ash', role, exp })}.sig`;
}

function renderAt(route: string) {
  return renderWithProviders(
    <Routes>
      <Route path="/login" element={<p>Pantalla de login</p>} />
      <Route element={<ProtectedRoute />}>
        <Route path="/mis-pokemon" element={<p>Mis Pokémon</p>} />
      </Route>
    </Routes>,
    { route },
  );
}

describe('ProtectedRoute', () => {
  it('redirects anonymous users to the login page', () => {
    renderAt('/mis-pokemon');

    expect(screen.getByText('Pantalla de login')).toBeInTheDocument();
    expect(screen.queryByText('Mis Pokémon')).not.toBeInTheDocument();
  });

  it('renders the protected page when a valid session is stored', () => {
    sessionStorage.setItem(SESSION_KEY, tokenFor('USER'));

    renderAt('/mis-pokemon');

    expect(screen.getByText('Mis Pokémon')).toBeInTheDocument();
  });

  it('ignores an expired session', () => {
    sessionStorage.setItem(SESSION_KEY, tokenFor('USER', 1));

    renderAt('/mis-pokemon');

    expect(screen.getByText('Pantalla de login')).toBeInTheDocument();
  });
});
