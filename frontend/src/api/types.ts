// Response shapes of the backend. They mirror the Java records in infrastructure/adapter/in/web.

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AbilityInfo {
  name: string;
  hidden: boolean;
}

export interface TypeInfo {
  name: string;
  slot: number;
}

export interface StatInfo {
  name: string;
  baseValue: number;
}

export interface EvolutionStage {
  speciesName: string;
  pokeApiId: number;
  stage: number;
  minLevel: number | null;
  trigger: string | null;
}

export interface PokemonSummary {
  id: number;
  name: string;
  spriteUrl: string | null;
  category: string;
  weight: number;
  abilities: AbilityInfo[];
}

export interface PokemonDetail {
  id: number;
  name: string;
  imageUrl: string | null;
  height: number;
  weight: number;
  types: TypeInfo[];
  stats: StatInfo[];
  description: string;
  evolutionChain: EvolutionStage[];
}

export interface LocalPokemon {
  id: number;
  pokeApiId: number;
  name: string;
  spriteUrl: string | null;
  category: string;
  height: number | null;
  weight: number | null;
  types: TypeInfo[];
  abilities: AbilityInfo[];
  localName: string | null;
  region: string | null;
  tags: string[];
  version: number;
  createdAt: string;
  updatedAt: string;
}

/** Own fields that users edit (PUT body). */
export interface LocalPokemonInput {
  localName: string;
  region: string | null;
  tags: string[];
}

export interface SyncSummary {
  synced: number;
  updated: number;
  failed: { id: number; reason: string }[];
}

export type Role = 'USER' | 'ADMIN';

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  role: Role;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
}

/** RFC 7807 body returned by every backend error. */
export interface ProblemDetail {
  title?: string;
  detail?: string;
  status?: number;
  correlationId?: string;
  errors?: { field: string; message: string }[];
}
