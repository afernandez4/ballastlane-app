const COLORS: Record<string, string> = {
  fire: 'bg-orange-500',
  water: 'bg-sky-600',
  grass: 'bg-emerald-600',
  electric: 'bg-yellow-400 text-slate-900',
  poison: 'bg-purple-600',
  bug: 'bg-lime-600',
  normal: 'bg-slate-400',
  flying: 'bg-indigo-400',
  ground: 'bg-amber-700',
  rock: 'bg-stone-500',
  psychic: 'bg-pink-500',
  ice: 'bg-cyan-400 text-slate-900',
  ghost: 'bg-violet-700',
  dragon: 'bg-indigo-700',
  dark: 'bg-neutral-800',
  steel: 'bg-slate-500',
  fairy: 'bg-rose-400 text-slate-900',
  fighting: 'bg-red-700',
};

export function TypeBadge({ name }: { name: string }) {
  return (
    <span
      className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold text-white ${
        COLORS[name] ?? 'bg-slate-500'
      }`}
    >
      {name}
    </span>
  );
}
