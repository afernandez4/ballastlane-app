import { Link } from 'react-router-dom';
import { capitalize, formatWeight } from '../features/format';
import type { AbilityInfo } from '../api/types';

interface PokemonCardProps {
  id: number;
  name: string;
  spriteUrl: string | null;
  category: string;
  weight: number;
  abilities: AbilityInfo[];
}

export function PokemonCard({
  id,
  name,
  spriteUrl,
  category,
  weight,
  abilities,
}: PokemonCardProps) {
  return (
    <Link
      to={`/pokedex/${id}`}
      className="group flex flex-col rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-700"
    >
      <div className="flex h-28 items-center justify-center">
        {spriteUrl ? (
          <img src={spriteUrl} alt="" loading="lazy" className="h-full object-contain" />
        ) : (
          <span className="text-sm text-slate-400">Sin imagen</span>
        )}
      </div>
      <p className="mt-3 text-xs font-mono text-slate-500">#{String(id).padStart(3, '0')}</p>
      <h2 className="text-lg font-semibold text-slate-900">{capitalize(name)}</h2>
      <p className="text-sm text-slate-600">{category || 'Pokémon'}</p>
      <dl className="mt-3 grid grid-cols-2 gap-2 text-sm">
        <div>
          <dt className="text-slate-500">Peso</dt>
          <dd className="font-medium">{formatWeight(weight)}</dd>
        </div>
        <div>
          <dt className="text-slate-500">Habilidades</dt>
          <dd className="truncate font-medium">
            {abilities
              .map((a) => a.name)
              .slice(0, 2)
              .join(', ') || '—'}
          </dd>
        </div>
      </dl>
    </Link>
  );
}
