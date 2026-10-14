import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { deleteLocalPokemon, listLocalPokemon } from '../api/local';
import { ApiError } from '../api/client';
import { AdminOnly } from '../auth/AdminOnly';
import { useAuth } from '../auth/useAuth';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { ErrorState } from '../components/ErrorState';
import { Pagination } from '../components/Pagination';
import { Spinner } from '../components/Spinner';
import { TypeBadge } from '../components/TypeBadge';
import { useToast } from '../components/useToast';
import { capitalize } from '../features/format';
import type { LocalPokemon } from '../api/types';

const PAGE_SIZE = 10;

/** US04 list: the user's local Pokémon, with search, filters and paging. Deleting is ADMIN only. */
export function MyPokemonPage() {
  const [params, setParams] = useSearchParams();
  const { isAuthenticated } = useAuth();
  const toast = useToast();
  const queryClient = useQueryClient();
  const [pendingDelete, setPendingDelete] = useState<LocalPokemon | null>(null);

  const page = Math.max(0, Number(params.get('page') ?? '0') || 0);
  const name = params.get('name') ?? '';
  const region = params.get('region') ?? '';
  const tag = params.get('tag') ?? '';

  const updateFilter = (key: 'name' | 'region' | 'tag', value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    next.delete('page');
    setParams(next);
  };

  const query = useQuery({
    queryKey: ['local', 'list', { page, name, region, tag }],
    queryFn: () => listLocalPokemon({ page, size: PAGE_SIZE, name, region, tag }),
    placeholderData: keepPreviousData,
  });

  const remove = useMutation({
    mutationFn: (id: number) => deleteLocalPokemon(id),
    onSuccess: () => {
      toast.show('Pokémon eliminado');
      setPendingDelete(null);
      void queryClient.invalidateQueries({ queryKey: ['local'] });
    },
    onError: (error) => {
      toast.show(error instanceof ApiError ? error.message : 'No se pudo eliminar', 'error');
      setPendingDelete(null);
    },
  });

  return (
    <section aria-labelledby="mine-title" className="space-y-6">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 id="mine-title" className="text-2xl font-bold tracking-tight">
            Mis Pokémon
          </h1>
          <p className="text-sm text-slate-600">Catálogo local con tus propios datos.</p>
        </div>
      </header>

      <form
        role="search"
        aria-label="Buscar Pokémon locales"
        className="grid gap-3 rounded-xl bg-white p-4 shadow-sm sm:grid-cols-3"
        onSubmit={(event) => event.preventDefault()}
      >
        <label className="block text-sm font-medium text-slate-800">
          Nombre
          <input
            type="search"
            value={name}
            onChange={(event) => updateFilter('name', event.target.value)}
            placeholder="Nombre o nombre local"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="block text-sm font-medium text-slate-800">
          Región
          <input
            value={region}
            onChange={(event) => updateFilter('region', event.target.value)}
            placeholder="Kanto, Johto…"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
        <label className="block text-sm font-medium text-slate-800">
          Etiqueta
          <input
            value={tag}
            onChange={(event) => updateFilter('tag', event.target.value)}
            placeholder="starter, fire…"
            className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2"
          />
        </label>
      </form>

      {query.isPending && <Spinner label="Cargando tus Pokémon" />}

      {query.isError && (
        <ErrorState
          message={query.error instanceof ApiError ? query.error.message : 'Error inesperado'}
          onRetry={() => void query.refetch()}
        />
      )}

      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No hay resultados"
          description="Guarda Pokémon desde el catálogo o cambia los filtros."
        >
          <Link to="/pokedex" className="font-medium text-slate-900 underline">
            Ir al catálogo
          </Link>
        </EmptyState>
      )}

      {query.isSuccess && query.data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-xl bg-white shadow-sm">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <caption className="sr-only">Pokémon locales</caption>
              <thead className="bg-slate-50 text-left text-slate-600">
                <tr>
                  <th scope="col" className="px-4 py-3 font-medium">
                    Pokémon
                  </th>
                  <th scope="col" className="px-4 py-3 font-medium">
                    Nombre local
                  </th>
                  <th scope="col" className="px-4 py-3 font-medium">
                    Región
                  </th>
                  <th scope="col" className="px-4 py-3 font-medium">
                    Etiquetas
                  </th>
                  <th scope="col" className="px-4 py-3">
                    <span className="sr-only">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {query.data.content.map((pokemon) => (
                  <tr key={pokemon.id}>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        {pokemon.spriteUrl && (
                          <img
                            src={pokemon.spriteUrl}
                            alt=""
                            className="h-10 w-10 object-contain"
                          />
                        )}
                        <div>
                          <p className="font-medium">{capitalize(pokemon.name)}</p>
                          <div className="mt-1 flex gap-1">
                            {pokemon.types.map((type) => (
                              <TypeBadge key={type.slot} name={type.name} />
                            ))}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3">{pokemon.localName ?? '—'}</td>
                    <td className="px-4 py-3">{pokemon.region ?? '—'}</td>
                    <td className="px-4 py-3">{pokemon.tags.join(', ') || '—'}</td>
                    <td className="px-4 py-3 text-right whitespace-nowrap">
                      {isAuthenticated && (
                        <Link
                          to={`/mis-pokemon/${pokemon.id}/editar`}
                          className="mr-3 font-medium text-slate-900 underline"
                        >
                          Editar<span className="sr-only"> {pokemon.name}</span>
                        </Link>
                      )}
                      <AdminOnly>
                        <button
                          type="button"
                          onClick={() => setPendingDelete(pokemon)}
                          className="font-medium text-red-700 underline"
                        >
                          Borrar<span className="sr-only"> {pokemon.name}</span>
                        </button>
                      </AdminOnly>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination
            page={query.data.page}
            totalPages={query.data.totalPages}
            onPageChange={(next) => {
              const copy = new URLSearchParams(params);
              if (next > 0) copy.set('page', String(next));
              else copy.delete('page');
              setParams(copy);
            }}
          />
        </>
      )}

      <ConfirmDialog
        open={pendingDelete !== null}
        title="¿Borrar este Pokémon?"
        description={`Se eliminará ${pendingDelete ? capitalize(pendingDelete.name) : ''} de tu catálogo local. Esta acción no se puede deshacer.`}
        confirmLabel="Borrar"
        busy={remove.isPending}
        onConfirm={() => pendingDelete && remove.mutate(pendingDelete.id)}
        onCancel={() => setPendingDelete(null)}
      />
    </section>
  );
}
