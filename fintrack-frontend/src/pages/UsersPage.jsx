import { useCallback, useEffect, useState } from "react";
import Table from "../components/Table";
import Pagination from "../components/Pagination";
import Modal from "../components/Modal";
import FormInput from "../components/FormInput";
import FormSelect from "../components/FormSelect";
import Loader from "../components/Loader";
import { useToast } from "../context/ToastContext";
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
} from "../services/userService";
import { normalizeRole } from "../utils/rbac";

const emptyForm = {
  email: "",
  password: "",
  firstName: "",
  lastName: "",
  role: "EMPLOYEE",
};

const UsersPage = () => {
  const { success, error: showError } = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getUsers({ page, size: pageSize });
      const body = res.data;
      const content = body?.content ?? body ?? [];
      setItems(Array.isArray(content) ? content : []);
      setTotalPages(body?.totalPages ?? 1);
      setTotalElements(
        typeof body?.totalElements === "number" ? body.totalElements : content.length
      );
    } catch {
      showError("Failed to load users");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, showError]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm({ ...emptyForm, password: "" });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      email: row.email || "",
      password: "",
      firstName: row.firstName || "",
      lastName: row.lastName || "",
      role: normalizeRole(row.role),
    });
    setModalOpen(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.email.trim()) {
      showError("Email is required");
      return;
    }
    if (!editing && !form.password) {
      showError("Password is required for new users");
      return;
    }
    const payload = {
      email: form.email.trim(),
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      role: form.role,
    };
    if (form.password) payload.password = form.password;

    setSaving(true);
    try {
      if (editing?.id != null) {
        await updateUser(editing.id, payload);
        success("User updated");
      } else {
        await createUser(payload);
        success("User created");
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Save failed";
      showError(typeof msg === "string" ? msg : "Save failed");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete user ${row.email}?`)) return;
    try {
      await deleteUser(row.id);
      success("User deleted");
      await load();
    } catch {
      showError("Delete failed");
    }
  };

  const columns = [
    {
      key: "email",
      header: "Email",
    },
    {
      key: "name",
      header: "Name",
      render: (r) =>
        [r.firstName, r.lastName].filter(Boolean).join(" ") || "—",
    },
    {
      key: "role",
      header: "Role",
      render: (r) => (
        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-800 dark:bg-slate-800 dark:text-slate-200">
          {normalizeRole(r.role)}
        </span>
      ),
    },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <div className="flex gap-2">
          <button
            type="button"
            className="text-xs font-semibold text-indigo-600 hover:underline dark:text-indigo-400"
            onClick={() => openEdit(r)}
          >
            Edit
          </button>
          <button
            type="button"
            className="text-xs font-semibold text-rose-600 hover:underline dark:text-rose-400"
            onClick={() => handleDelete(r)}
          >
            Delete
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Users</h1>
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Admin-only directory. Create and maintain accounts and roles.
          </p>
        </div>
        <button
          type="button"
          onClick={openCreate}
          className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500"
        >
          New user
        </button>
      </div>

      {loading ? (
        <Loader label="Loading users" />
      ) : (
        <>
          <Table columns={columns} data={items} emptyMessage="No users found." />
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={pageSize}
            onPageChange={setPage}
            onPageSizeChange={(s) => {
              setPageSize(s);
              setPage(0);
            }}
          />
        </>
      )}

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? "Edit user" : "New user"}
        footer={
          <>
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-800 dark:border-slate-600 dark:text-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              form="user-form"
              disabled={saving}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {saving ? "Saving…" : "Save"}
            </button>
          </>
        }
      >
        <form id="user-form" className="space-y-4" onSubmit={handleSave}>
          <FormInput
            label="Email"
            name="email"
            type="email"
            value={form.email}
            onChange={handleFormChange}
            required
            disabled={Boolean(editing)}
          />
          <FormInput
            label={editing ? "New password (optional)" : "Password"}
            name="password"
            type="password"
            value={form.password}
            onChange={handleFormChange}
            required={!editing}
            autoComplete="new-password"
          />
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <FormInput
              label="First name"
              name="firstName"
              value={form.firstName}
              onChange={handleFormChange}
            />
            <FormInput
              label="Last name"
              name="lastName"
              value={form.lastName}
              onChange={handleFormChange}
            />
          </div>
          <FormSelect
            label="Role"
            name="role"
            value={form.role}
            onChange={handleFormChange}
            required
          >
            <option value="EMPLOYEE">Employee</option>
            <option value="MANAGER">Manager</option>
            <option value="ADMIN">Admin</option>
          </FormSelect>
        </form>
      </Modal>
    </div>
  );
};

export default UsersPage;
