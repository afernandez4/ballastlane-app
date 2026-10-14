interface PaginationProps {
  /** Zero-based page index, as in the API. */
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

const buttonClass =
  'rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-700';

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  const lastPage = Math.max(totalPages - 1, 0);
  return (
    <nav aria-label="Paginación" className="flex items-center justify-between gap-4 py-4">
      <button
        type="button"
        className={buttonClass}
        onClick={() => onPageChange(page - 1)}
        disabled={page <= 0}
      >
        Anterior
      </button>
      <span className="text-sm text-slate-600" aria-live="polite">
        Página {totalPages === 0 ? 0 : page + 1} de {totalPages}
      </span>
      <button
        type="button"
        className={buttonClass}
        onClick={() => onPageChange(page + 1)}
        disabled={page >= lastPage}
      >
        Siguiente
      </button>
    </nav>
  );
}
