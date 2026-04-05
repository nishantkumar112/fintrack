import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";

const ToastContext = createContext(null);

let idSeq = 0;

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((list) => list.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (message, variant = "info") => {
      const id = ++idSeq;
      setToasts((list) => [...list, { id, message, variant }]);
      window.setTimeout(() => removeToast(id), 4500);
    },
    [removeToast]
  );

  const value = useMemo(
    () => ({
      showToast,
      success: (m) => showToast(m, "success"),
      error: (m) => showToast(m, "error"),
      info: (m) => showToast(m, "info"),
    }),
    [showToast]
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div
        className="pointer-events-none fixed right-4 top-4 z-[100] flex w-full max-w-sm flex-col gap-2"
        aria-live="polite"
      >
        {toasts.map((t) => (
          <div
            key={t.id}
            role="status"
            className={`pointer-events-auto rounded-lg border px-4 py-3 text-sm shadow-lg backdrop-blur-sm ${
              t.variant === "success"
                ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-900 dark:text-emerald-100"
                : t.variant === "error"
                  ? "border-red-500/30 bg-red-500/10 text-red-900 dark:text-red-100"
                  : "border-slate-500/30 bg-slate-500/10 text-slate-900 dark:text-slate-100"
            }`}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within ToastProvider");
  return ctx;
};
