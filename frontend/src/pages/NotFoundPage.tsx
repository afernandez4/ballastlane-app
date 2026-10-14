import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="py-16 text-center">
      <h1 className="text-3xl font-bold">Page not found</h1>
      <p className="mt-2 text-slate-600">The address does not exist or is no longer available.</p>
      <Link to="/pokedex" className="mt-6 inline-block font-medium text-slate-900 underline">
        Go to the Pokédex
      </Link>
    </section>
  );
}
