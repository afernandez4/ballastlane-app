/** Placeholder cards shown while the first page loads. */
export function CardSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div
      className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4"
      role="status"
      aria-label="Cargando Pokémon"
    >
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="h-64 animate-pulse rounded-xl bg-slate-200" />
      ))}
    </div>
  );
}
