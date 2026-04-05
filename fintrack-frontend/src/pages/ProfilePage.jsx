import { useAuth } from "../context/AuthContext";
import { normalizeRole } from "../utils/rbac";

const ProfilePage = () => {
  const { user } = useAuth();
  const role = normalizeRole(user?.role);

  const rows = [
    { label: "Email", value: user?.email || "—" },
    { label: "First name", value: user?.firstName || "—" },
    { label: "Last name", value: user?.lastName || "—" },
    { label: "Role", value: role },
    { label: "User ID", value: user?.id != null ? String(user.id) : "—" },
  ];

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Profile</h1>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Information from your session. Update flows can be wired to{" "}
          <code className="rounded bg-slate-100 px-1 text-xs dark:bg-slate-800">PUT /users/&#123;id&#125;</code>{" "}
          when the backend exposes self-service edits.
        </p>
      </div>
      <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <dl className="divide-y divide-slate-100 dark:divide-slate-800">
          {rows.map((r) => (
            <div key={r.label} className="grid grid-cols-3 gap-4 px-4 py-3 sm:px-6">
              <dt className="text-sm font-medium text-slate-500 dark:text-slate-400">{r.label}</dt>
              <dd className="col-span-2 text-sm text-slate-900 dark:text-slate-100">{r.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  );
};

export default ProfilePage;
