const pageSizeOptions = [10, 20, 50];

const Pagination = ({
  page,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange,
}) => {
  const safeTotalPages = Math.max(1, totalPages || 1);
  const current = Math.min(page, safeTotalPages - 1);

  const handlePrev = () => onPageChange(Math.max(0, current - 1));
  const handleNext = () =>
    onPageChange(Math.min(safeTotalPages - 1, current + 1));

  return (
    <div className="flex flex-col gap-3 border-t border-slate-200 pt-4 text-sm text-slate-600 dark:border-slate-700 dark:text-slate-300 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex flex-wrap items-center gap-2">
        <span>
          Page <strong className="text-slate-900 dark:text-white">{current + 1}</strong> of{" "}
          <strong className="text-slate-900 dark:text-white">{safeTotalPages}</strong>
          {typeof totalElements === "number" ? (
            <span className="text-slate-500 dark:text-slate-400">
              {" "}
              ({totalElements} total)
            </span>
          ) : null}
        </span>
        <label className="ml-2 flex items-center gap-2 text-xs">
          <span className="sr-only">Rows per page</span>
          <span>Rows</span>
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            className="rounded-md border border-slate-200 bg-white px-2 py-1 text-slate-900 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
          >
            {pageSizeOptions.map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </label>
      </div>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={handlePrev}
          disabled={current <= 0}
          className="rounded-lg border border-slate-200 px-3 py-1.5 font-medium text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-600 dark:text-slate-100 dark:hover:bg-slate-800"
        >
          Previous
        </button>
        <button
          type="button"
          onClick={handleNext}
          disabled={current >= safeTotalPages - 1}
          className="rounded-lg border border-slate-200 px-3 py-1.5 font-medium text-slate-800 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-600 dark:text-slate-100 dark:hover:bg-slate-800"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default Pagination;
