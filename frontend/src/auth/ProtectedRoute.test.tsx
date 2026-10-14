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
      <Route path="/login" element={<p>Login screen</p>} />
      <Route element={<ProtectedRoute />}>
        <Route path="/my-pokemon" element={<p>My Pokémon</p>} />
      </Route>
    </Routes>,
    { route },
  );
}

describe('ProtectedRoute', () => {
  it('redirects anonymous users to the login page', () => {
    renderAt('/my-pokemon');

    expect(screen.getByText('Login screen')).toBeInTheDocument();
    expect(screen.queryByText('My Pokémon')).not.toBeInTheDocument();
  });

  it('renders the protected page when a valid session is stored', () => {
    sessionStorage.setItem(SESSION_KEY, tokenFor('USER'));

    renderAt('/my-pokemon');

    expect(screen.getByText('My Pokémon')).toBeInTheDocument();
  });

  it('ignores an expired session', () => {
    sessionStorage.setItem(SESSION_KEY, tokenFor('USER', 1));

    renderAt('/my-pokemon');

    expect(screen.getByText('Login screen')).toBeInTheDocument();
  });
});
