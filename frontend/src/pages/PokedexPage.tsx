import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { listPokemon } from '../api/catalog';
import { ApiError } from '../api/client';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { PokemonCard } from '../components/PokemonCard';
import { Pagination } from '../components/Pagination';
import { CardSkeleton } from '../components/Skeleton';

const PAGE_SIZE = 20;

/** US01: paginated catalogue from PokeAPI. The page lives in the URL so it can be shared. */
export function PokedexPage() {
  const [params, setParams] = useSearchParams();
  const page = Math.max(0, Number(params.get('page') ?? '0') || 0);

  const query = useQuery({
    queryKey: ['pokeapi', 'list', page, PAGE_SIZE],
    queryFn: () => listPokemon(page, PAGE_SIZE),
    placeholderData: keepPreviousData,
  });

  const goToPage = (next: number) => {
    setParams(next > 0 ? { page: String(next) } : {});
    window.scrollTo({ top: 0 });
  };

  return (
    <section aria-labelledby="pokedex-title" className="space-y-6">
      <header>
        <h1 id="pokedex-title" className="text-2xl font-bold tracking-tight">
          Pokédex
        </h1>
        <p className="text-sm text-slate-600">Datos de PokeAPI, con paginación.</p>
      </header>

      {query.isPending && <CardSkeleton count={PAGE_SIZE > 8 ? 8 : PAGE_SIZE} />}

      {query.isError && (
        <ErrorState
          message={query.error instanceof ApiError ? query.error.message : 'Error inesperado'}
          onRetry={() => void query.refetch()}
        />
      )}

      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No hay Pokémon en esta página"
          description="Prueba con una página anterior."
        />
      )}

      {query.isSuccess && query.data.content.length > 0 && (
        <>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {query.data.content.map((pokemon) => (
              <li key={pokemon.id}>
                <PokemonCard
                  id={pokemon.id}
                  name={pokemon.name}
                  spriteUrl={pokemon.spriteUrl}
                  category={pokemon.category}
                  weight={pokemon.weight}
                  abilities={pokemon.abilities}
                />
              </li>
            ))}
          </ul>
          <Pagination
            page={query.data.page}
            totalPages={query.data.totalPages}
            onPageChange={goToPage}
          />
        </>
      )}
    </section>
  );
}
