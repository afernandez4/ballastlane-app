import { api } from './client';
import type { Page, PokemonDetail, PokemonSummary } from './types';

export async function listPokemon(page: number, size: number): Promise<Page<PokemonSummary>> {
  const { data } = await api.get<Page<PokemonSummary>>('/api/v1/pokeapi/pokemon', {
    params: { page, size },
  });
  return data;
}

export async function getPokemonDetail(idOrName: string): Promise<PokemonDetail> {
  const { data } = await api.get<PokemonDetail>(
    `/api/v1/pokeapi/pokemon/${encodeURIComponent(idOrName)}`,
  );
  return data;
}
