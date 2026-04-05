import { Outlet } from "react-router-dom";
import { Link } from "react-router-dom";

const AuthLayout = () => {
  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-indigo-50 via-white to-slate-100 dark:from-slate-950 dark:via-slate-950 dark:to-indigo-950">
      <div className="pointer-events-none absolute -left-24 top-0 h-72 w-72 rounded-full bg-indigo-400/20 blur-3xl dark:bg-indigo-600/20" />
      <div className="pointer-events-none absolute bottom-0 right-0 h-80 w-80 rounded-full bg-violet-400/20 blur-3xl dark:bg-violet-600/10" />
      <div className="relative mx-auto flex min-h-screen max-w-lg flex-col justify-center px-4 py-12">
        <Link
          to="/login"
          className="mb-8 text-center text-2xl font-bold tracking-tight text-slate-900 dark:text-white"
        >
          FinTrack
        </Link>
        <Outlet />
      </div>
    </div>
  );
};

export default AuthLayout;
