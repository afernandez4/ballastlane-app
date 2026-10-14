/** PokeAPI weights are in hectograms and heights in decimetres. */
export function formatWeight(hectograms: number | null | undefined): string {
  return hectograms == null ? '—' : `${(hectograms / 10).toFixed(1)} kg`;
}

export function formatHeight(decimetres: number | null | undefined): string {
  return decimetres == null ? '—' : `${(decimetres / 10).toFixed(1)} m`;
}

export function capitalize(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1);
}
