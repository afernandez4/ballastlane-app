import { api } from './client';
import type { LocalPokemon, LocalPokemonInput, Page } from './types';

export interface LocalFilters {
  page: number;
  size: number;
  name?: string;
  region?: string;
  tag?: string;
}

/** A resource plus its ETag, which is the version to send back in If-Match. */
export interface Versioned<T> {
  data: T;
  etag: string | undefined;
}

export async function listLocalPokemon(filters: LocalFilters): Promise<Page<LocalPokemon>> {
  const { data } = await api.get<Page<LocalPokemon>>('/api/v1/pokemon', { params: filters });
  return data;
}

export async function getLocalPokemon(id: number): Promise<Versioned<LocalPokemon>> {
  const response = await api.get<LocalPokemon>(`/api/v1/pokemon/${id}`);
  return { data: response.data, etag: response.headers['etag'] };
}

export async function updateLocalPokemon(
  id: number,
  input: LocalPokemonInput,
  etag: string | undefined,
): Promise<Versioned<LocalPokemon>> {
  const response = await api.put<LocalPokemon>(`/api/v1/pokemon/${id}`, input, {
    headers: etag ? { 'If-Match': etag } : {},
  });
  return { data: response.data, etag: response.headers['etag'] };
}

export async function deleteLocalPokemon(id: number): Promise<void> {
  await api.delete(`/api/v1/pokemon/${id}`);
}

export interface SyncResult {
  pokemon: LocalPokemon;
  created: boolean;
}

/** 201 when the row was created, 200 when it already existed and was refreshed. */
export async function syncPokemon(idOrName: string): Promise<SyncResult> {
  const response = await api.post<LocalPokemon>(
    `/api/v1/pokemon/sync/${encodeURIComponent(idOrName)}`,
  );
  return { pokemon: response.data, created: response.status === 201 };
}
