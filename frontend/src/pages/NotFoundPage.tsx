import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <section className="py-16 text-center">
      <h1 className="text-3xl font-bold">Página no encontrada</h1>
      <p className="mt-2 text-slate-600">La dirección no existe o ya no está disponible.</p>
      <Link to="/pokedex" className="mt-6 inline-block font-medium text-slate-900 underline">
        Ir a la Pokédex
      </Link>
    </section>
  );
}
