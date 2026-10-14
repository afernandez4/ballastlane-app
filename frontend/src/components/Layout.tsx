import { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-700 ${
    isActive ? 'bg-slate-900 text-white' : 'text-slate-700 hover:bg-slate-200'
  }`;

export function Layout() {
  const { isAuthenticated, claims, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    setMenuOpen(false);
    navigate('/pokedex');
  };

  const links = (
    <>
      <NavLink to="/pokedex" className={linkClass} onClick={() => setMenuOpen(false)}>
        Pokédex
      </NavLink>
      <NavLink to="/my-pokemon" className={linkClass} onClick={() => setMenuOpen(false)}>
        My Pokémon
      </NavLink>
    </>
  );

  const account = isAuthenticated ? (
    <div className="flex items-center gap-3">
      <span className="text-sm text-slate-600">
        {claims?.sub} {claims?.role === 'ADMIN' && <span className="font-semibold">(admin)</span>}
      </span>
      <button
        type="button"
        onClick={handleLogout}
        className="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium hover:bg-slate-100"
      >
        Log out
      </button>
    </div>
  ) : (
    <div className="flex items-center gap-2">
      <NavLink to="/login" className={linkClass}>
        Entrar
      </NavLink>
      <NavLink to="/register" className={linkClass}>
        Sign up
      </NavLink>
    </div>
  );

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/pokedex" className="text-lg font-bold tracking-tight text-slate-900">
            Pokédex
          </Link>

          <nav aria-label="Main" className="hidden items-center gap-1 md:flex">
            {links}
          </nav>
          <div className="hidden md:block">{account}</div>

          <button
            type="button"
            className="rounded-md p-2 text-slate-700 hover:bg-slate-100 md:hidden focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-700"
            aria-expanded={menuOpen}
            aria-controls="mobile-menu"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span aria-hidden="true" className="block text-xl leading-none">
              {menuOpen ? '✕' : '☰'}
            </span>
          </button>
        </div>

        {menuOpen && (
          <nav
            id="mobile-menu"
            aria-label="Mobile"
            className="border-t border-slate-200 px-4 py-3 md:hidden"
          >
            <div className="flex flex-col gap-1">{links}</div>
            <div className="mt-3 border-t border-slate-200 pt-3">{account}</div>
          </nav>
        )}
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
