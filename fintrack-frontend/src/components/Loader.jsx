const Loader = ({ fullPage = false, label = "Loading" }) => {
  const spinner = (
    <div
      className="h-10 w-10 animate-spin rounded-full border-2 border-slate-300 border-t-indigo-600 dark:border-slate-600 dark:border-t-indigo-400"
      role="progressbar"
      aria-label={label}
    />
  );

  if (fullPage) {
    return (
      <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3 text-slate-600 dark:text-slate-300">
        {spinner}
        <span className="text-sm">{label}</span>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center py-8 text-slate-600 dark:text-slate-300">
      {spinner}
    </div>
  );
};

export default Loader;
