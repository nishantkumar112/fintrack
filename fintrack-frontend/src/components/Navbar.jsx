import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";

const Navbar = ({ onMenuClick }) => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
  };

  const handleThemeKeyDown = (e) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      toggleTheme();
    }
  };

  return (
    <header className="sticky top-0 z-40 border-b border-slate-200/80 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
      <div className="flex h-14 items-center justify-between gap-4 px-4 lg:px-6">
        <div className="flex items-center gap-3">
          <button
            type="button"
            className="inline-flex rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden dark:text-slate-300 dark:hover:bg-slate-800"
            aria-label="Open navigation menu"
            onClick={onMenuClick}
          >
            <span className="text-xl" aria-hidden>
              ☰
            </span>
          </button>
          <Link to="/dashboard" className="text-lg font-semibold text-slate-900 dark:text-white">
            FinTrack
          </Link>
        </div>
        <div className="flex items-center gap-2">
          <div
            role="button"
            tabIndex={0}
            onClick={toggleTheme}
            onKeyDown={handleThemeKeyDown}
            className="cursor-pointer rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-medium text-slate-700 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-200 dark:hover:bg-slate-800"
            aria-label={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
          >
            {theme === "dark" ? "Light" : "Dark"}
          </div>
          <span className="hidden text-sm text-slate-600 sm:inline dark:text-slate-300">
            {user?.email || user?.firstName || "User"}
          </span>
          <button
            type="button"
            onClick={handleLogout}
            className="rounded-lg bg-slate-900 px-3 py-1.5 text-xs font-semibold text-white hover:bg-slate-800 dark:bg-indigo-600 dark:hover:bg-indigo-500"
          >
            Log out
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
