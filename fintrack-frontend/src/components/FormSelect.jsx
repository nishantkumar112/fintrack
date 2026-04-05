const FormSelect = ({
  id,
  label,
  name,
  value,
  onChange,
  children,
  error,
  required,
  disabled,
  className = "",
}) => {
  const selectId = id || name || label?.replace(/\s+/g, "-").toLowerCase();

  return (
    <div className={`space-y-1 ${className}`}>
      {label ? (
        <label
          htmlFor={selectId}
          className="block text-sm font-medium text-slate-700 dark:text-slate-200"
        >
          {label}
          {required ? <span className="text-red-500"> *</span> : null}
        </label>
      ) : null}
      <select
        id={selectId}
        name={name}
        value={value}
        onChange={onChange}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${selectId}-error` : undefined}
        className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none ring-indigo-500/0 transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/30 disabled:cursor-not-allowed disabled:opacity-60 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
      >
        {children}
      </select>
      {error ? (
        <p id={`${selectId}-error`} className="text-xs text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}
    </div>
  );
};

export default FormSelect;
