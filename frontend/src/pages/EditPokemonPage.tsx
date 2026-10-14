import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getLocalPokemon, updateLocalPokemon } from '../api/local';
import { ApiError } from '../api/client';
import { ErrorState } from '../components/ErrorState';
import { EmptyState } from '../components/EmptyState';
import { Spinner } from '../components/Spinner';
import { useToast } from '../components/useToast';
import { capitalize } from '../features/format';
import { localPokemonSchema, parseTags, type LocalPokemonForm } from '../features/schemas';
import { FieldError, Label } from './formParts';
import { fieldClass } from '../features/formStyles';

/** US04: edit the own fields. 400 maps to field errors, 404 and 409 get their own messages. */
export function EditPokemonPage() {
  const { id = '' } = useParams();
  const pokemonId = Number(id);
  const navigate = useNavigate();
  const toast = useToast();
  const queryClient = useQueryClient();
  const [conflict, setConflict] = useState(false);

  const query = useQuery({
    queryKey: ['local', 'one', pokemonId],
    queryFn: () => getLocalPokemon(pokemonId),
    enabled: Number.isInteger(pokemonId) && pokemonId > 0,
  });

  const form = useForm<LocalPokemonForm>({
    resolver: zodResolver(localPokemonSchema),
    defaultValues: { localName: '', region: '', tagsText: '' },
  });

  // Fill the form whenever fresh data arrives (first load, reload after a conflict).
  const loaded = query.data?.data;
  useEffect(() => {
    if (loaded) {
      form.reset({
        localName: loaded.localName ?? '',
        region: loaded.region ?? '',
        tagsText: loaded.tags.join(', '),
      });
    }
  }, [loaded, form]);

  const save = useMutation({
    mutationFn: (values: LocalPokemonForm) =>
      updateLocalPokemon(
        pokemonId,
        {
          localName: values.localName,
          region: values.region === '' ? null : values.region,
          tags: parseTags(values.tagsText),
        },
        query.data?.etag,
      ),
    onSuccess: () => {
      toast.show('Cambios guardados');
      void queryClient.invalidateQueries({ queryKey: ['local'] });
      navigate('/mis-pokemon');
    },
    onError: (error) => {
      if (!(error instanceof ApiError)) return;
      if (error.status === 400) {
        const mapped: Record<string, keyof LocalPokemonForm> = {
          localName: 'localName',
          region: 'region',
          tags: 'tagsText',
        };
        for (const [field, message] of Object.entries(error.fieldErrors)) {
          const target = mapped[field];
          if (target) form.setError(target, { type: 'server', message });
        }
      }
      if (error.status === 409) setConflict(true);
    },
  });

  if (query.isPending) {
    return <Spinner label="Cargando Pokémon" />;
  }

  if (query.isError) {
    if (query.error instanceof ApiError && query.error.status === 404) {
      return (
        <EmptyState title="Este Pokémon ya no existe">
          <Link to="/mis-pokemon" className="font-medium text-slate-900 underline">
            Volver a mis Pokémon
          </Link>
        </EmptyState>
      );
    }
    return (
      <ErrorState
        message={query.error instanceof ApiError ? query.error.message : 'Error inesperado'}
        onRetry={() => void query.refetch()}
      />
    );
  }

  const pokemon = query.data.data;
  const submitError =
    save.error instanceof ApiError && ![400, 409, 404].includes(save.error.status)
      ? 'No se pudieron guardar los cambios. Inténtalo de nuevo.'
      : null;

  return (
    <section aria-labelledby="edit-title" className="mx-auto max-w-xl space-y-6">
      <Link to="/mis-pokemon" className="text-sm font-medium text-slate-700 underline">
        ← Volver a mis Pokémon
      </Link>
      <header>
        <h1 id="edit-title" className="text-2xl font-bold tracking-tight">
          Editar {capitalize(pokemon.name)}
        </h1>
        <p className="text-sm text-slate-600">
          Los datos base vienen de PokeAPI. Solo puedes cambiar los campos propios.
        </p>
      </header>

      {conflict && (
        <ErrorState
          message="Otra persona modificó este Pokémon mientras editabas. Recarga los datos y vuelve a aplicar tus cambios."
          onRetry={() => {
            setConflict(false);
            void query.refetch();
          }}
        />
      )}
      {submitError && <ErrorState message={submitError} />}

      <form
        noValidate
        onSubmit={form.handleSubmit((values) => save.mutate(values))}
        className="space-y-4 rounded-xl bg-white p-6 shadow-sm"
      >
        <div>
          <Label htmlFor="localName">Nombre local</Label>
          <input
            id="localName"
            className={fieldClass(!!form.formState.errors.localName)}
            aria-invalid={!!form.formState.errors.localName}
            aria-describedby="localName-error"
            {...form.register('localName')}
          />
          <FieldError id="localName-error" message={form.formState.errors.localName?.message} />
        </div>
        <div>
          <Label htmlFor="region">Región</Label>
          <input
            id="region"
            className={fieldClass(!!form.formState.errors.region)}
            aria-invalid={!!form.formState.errors.region}
            aria-describedby="region-error"
            {...form.register('region')}
          />
          <FieldError id="region-error" message={form.formState.errors.region?.message} />
        </div>
        <div>
          <Label htmlFor="tagsText">Etiquetas</Label>
          <input
            id="tagsText"
            className={fieldClass(!!form.formState.errors.tagsText)}
            aria-invalid={!!form.formState.errors.tagsText}
            aria-describedby="tags-help tagsText-error"
            {...form.register('tagsText')}
          />
          <p id="tags-help" className="text-xs text-slate-500">
            Separadas por comas. Máximo 10, sin repetir, de hasta 30 caracteres.
          </p>
          <FieldError id="tagsText-error" message={form.formState.errors.tagsText?.message} />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <Link
            to="/mis-pokemon"
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-100"
          >
            Cancelar
          </Link>
          <button
            type="submit"
            disabled={save.isPending || conflict}
            className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-700 disabled:opacity-60"
          >
            {save.isPending ? 'Guardando…' : 'Guardar'}
          </button>
        </div>
      </form>
    </section>
  );
}
