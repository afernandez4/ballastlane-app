import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { getPokemonDetail } from '../api/catalog';
import { ApiError } from '../api/client';
import { syncPokemon } from '../api/local';
import { useAuth } from '../auth/useAuth';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { Spinner } from '../components/Spinner';
import { StatBar } from '../components/StatBar';
import { TypeBadge } from '../components/TypeBadge';
import { useToast } from '../components/useToast';
import { capitalize, formatHeight, formatWeight } from '../features/format';

/** US02 detail with the evolution chain, and US03 sync (needs a logged-in user). */
export function PokemonDetailPage() {
  const { idOrName = '' } = useParams();
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const toast = useToast();
  const queryClient = useQueryClient();

  const detail = useQuery({
    queryKey: ['pokeapi', 'detail', idOrName],
    queryFn: () => getPokemonDetail(idOrName),
    enabled: idOrName.length > 0,
    placeholderData: keepPreviousData,
  });

  const sync = useMutation({
    mutationFn: () => syncPokemon(idOrName),
    onSuccess: ({ created }) => {
      toast.show(created ? 'Pokémon saved to your catalogue' : 'Data updated');
      void queryClient.invalidateQueries({ queryKey: ['local'] });
    },
    onError: (error) => {
      toast.show(error instanceof ApiError ? error.message : 'Could not sync', 'error');
    },
  });

  const handleSync = () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location.pathname } });
      return;
    }
    sync.mutate();
  };

  if (detail.isPending) {
    return <Spinner label="Loading Pokémon" />;
  }

  if (detail.isError) {
    if (detail.error instanceof ApiError && detail.error.status === 404) {
      return (
        <EmptyState title="This Pokémon does not exist" description="Check the name or number.">
          <Link to="/pokedex" className="font-medium text-slate-900 underline">
            Back to the catalogue
          </Link>
        </EmptyState>
      );
    }
    return (
      <ErrorState
        message={detail.error instanceof ApiError ? detail.error.message : 'Error inesperado'}
        onRetry={() => void detail.refetch()}
      />
    );
  }

  const pokemon = detail.data;
  return (
    <article aria-labelledby="detail-title" className="space-y-8">
      <Link to="/pokedex" className="text-sm font-medium text-slate-700 underline">
        ← Back to the catalogue
      </Link>

      <header className="flex flex-col gap-6 md:flex-row md:items-center">
        <div className="flex h-56 w-full items-center justify-center rounded-xl bg-white md:w-72">
          {pokemon.imageUrl ? (
            <img
              src={pokemon.imageUrl}
              alt={`Image of ${capitalize(pokemon.name)}`}
              className="h-full object-contain p-4"
            />
          ) : (
            <span className="text-slate-400">No image</span>
          )}
        </div>
        <div className="flex-1 space-y-3">
          <p className="font-mono text-sm text-slate-500">#{String(pokemon.id).padStart(3, '0')}</p>
          <h1 id="detail-title" className="text-3xl font-bold tracking-tight">
            {capitalize(pokemon.name)}
          </h1>
          <div className="flex flex-wrap gap-2">
            {pokemon.types.map((type) => (
              <TypeBadge key={type.slot} name={type.name} />
            ))}
          </div>
          <dl className="grid grid-cols-2 gap-4 text-sm sm:max-w-sm">
            <div>
              <dt className="text-slate-500">Height</dt>
              <dd className="font-medium">{formatHeight(pokemon.height)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Weight</dt>
              <dd className="font-medium">{formatWeight(pokemon.weight)}</dd>
            </div>
          </dl>
          <div>
            <button
              type="button"
              onClick={handleSync}
              disabled={sync.isPending}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-900"
            >
              {sync.isPending ? 'Syncing…' : 'Save to my catalogue'}
            </button>
            {!isAuthenticated && (
              <p className="mt-2 text-xs text-slate-500">You need to log in to save it.</p>
            )}
          </div>
        </div>
      </header>

      {pokemon.description && (
        <section aria-labelledby="description-title" className="space-y-2">
          <h2 id="description-title" className="text-lg font-semibold">
            Description
          </h2>
          <p className="max-w-prose text-slate-700">{pokemon.description}</p>
        </section>
      )}

      <section aria-labelledby="stats-title" className="space-y-3">
        <h2 id="stats-title" className="text-lg font-semibold">
          Base stats
        </h2>
        <div className="max-w-xl space-y-2 rounded-xl bg-white p-4 shadow-sm">
          {pokemon.stats.map((stat) => (
            <StatBar key={stat.name} name={stat.name} value={stat.baseValue} />
          ))}
        </div>
      </section>

      {pokemon.evolutionChain.length > 0 && (
        <section aria-labelledby="evolution-title" className="space-y-3">
          <h2 id="evolution-title" className="text-lg font-semibold">
            Evolution chain
          </h2>
          <ol className="flex flex-wrap items-center gap-2">
            {pokemon.evolutionChain.map((stage, index) => (
              <li key={stage.pokeApiId} className="flex items-center gap-2">
                {index > 0 && (
                  <span aria-hidden="true" className="text-slate-400">
                    →{stage.minLevel ? ` nivel ${stage.minLevel}` : ''}
                  </span>
                )}
                <Link
                  to={`/pokedex/${stage.pokeApiId}`}
                  aria-current={stage.speciesName === pokemon.name ? 'page' : undefined}
                  className={`rounded-lg border px-3 py-1.5 text-sm font-medium capitalize ${
                    stage.speciesName === pokemon.name
                      ? 'border-slate-900 bg-slate-900 text-white'
                      : 'border-slate-300 bg-white text-slate-800 hover:bg-slate-100'
                  }`}
                >
                  {stage.speciesName}
                </Link>
              </li>
            ))}
          </ol>
        </section>
      )}
    </article>
  );
}
