import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isAdmin } from "../utils/rbac";

const linkClass = ({ isActive }) =>
  `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive
      ? "bg-indigo-600 text-white shadow-sm"
      : "text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800"
  }`;

const Sidebar = ({ open, onNavigate }) => {
  const { user } = useAuth();
  const showUsers = isAdmin(user);

  const handleNavClick = () => {
    onNavigate?.();
  };

  const nav = (
    <nav className="flex flex-col gap-1 p-3" aria-label="Main">
      <NavLink to="/dashboard" className={linkClass} onClick={handleNavClick}>
        <span aria-hidden>📊</span> Dashboard
      </NavLink>
      <NavLink to="/transactions" className={linkClass} onClick={handleNavClick}>
        <span aria-hidden>💳</span> Transactions
      </NavLink>
      {showUsers ? (
        <NavLink to="/users" className={linkClass} onClick={handleNavClick}>
          <span aria-hidden>👥</span> Users
        </NavLink>
      ) : null}
      <NavLink to="/profile" className={linkClass} onClick={handleNavClick}>
        <span aria-hidden>👤</span> Profile
      </NavLink>
    </nav>
  );

  return (
    <>
      <div
        className={`fixed inset-0 z-30 bg-slate-900/40 transition lg:hidden dark:bg-black/50 ${
          open ? "opacity-100 pointer-events-auto" : "pointer-events-none opacity-0"
        }`}
        aria-hidden={!open}
        onClick={onNavigate}
      />
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 transform border-r border-slate-200 bg-white transition duration-200 dark:border-slate-800 dark:bg-slate-950 lg:static lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-14 items-center border-b border-slate-200 px-4 dark:border-slate-800">
          <span className="text-sm font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
            Menu
          </span>
        </div>
        {nav}
      </aside>
    </>
  );
};

export default Sidebar;
