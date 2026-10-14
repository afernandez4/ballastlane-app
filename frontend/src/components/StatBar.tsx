interface StatBarProps {
  name: string;
  value: number;
}

/** Base stats are capped at 255 by the games; the bar is scaled to that. */
export function StatBar({ name, value }: StatBarProps) {
  const percent = Math.min(100, Math.round((value / 255) * 100));
  return (
    <div className="grid grid-cols-[7rem_2.5rem_1fr] items-center gap-3 text-sm">
      <span className="capitalize text-slate-700">{name.replace('-', ' ')}</span>
      <span className="text-right font-mono tabular-nums text-slate-900">{value}</span>
      <div
        role="meter"
        aria-label={`${name} ${value} of 255`}
        aria-valuemin={0}
        aria-valuemax={255}
        aria-valuenow={value}
        className="h-2.5 overflow-hidden rounded-full bg-slate-200"
      >
        <div className="h-full rounded-full bg-slate-800" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}
