import { useCallback, useEffect, useState } from "react";
import Table from "../components/Table";
import Pagination from "../components/Pagination";
import Modal from "../components/Modal";
import FormInput from "../components/FormInput";
import FormSelect from "../components/FormSelect";
import Loader from "../components/Loader";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { useDebounce } from "../hooks/useDebounce";
import {
  getTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
  approveTransaction,
  rejectTransaction,
} from "../services/transactionService";
import { formatCurrency, formatDate } from "../utils/format";
import { canApproveTransactions } from "../utils/rbac";
import { downloadTransactionsCsv } from "../utils/csv";

const emptyForm = {
  type: "EXPENSE",
  amount: "",
  category: "",
  description: "",
  transactionDate: "",
};

const TransactionsPage = () => {
  const { user } = useAuth();
  const { success, error: showError } = useToast();
  const canApprove = canApproveTransactions(user);

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [filterType, setFilterType] = useState("");
  const [filterCategory, setFilterCategory] = useState("");
  const [filterStatus, setFilterStatus] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const debouncedSearch = useDebounce(searchInput, 400);

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: pageSize,
      };
      if (filterType) params.type = filterType;
      if (filterCategory.trim()) params.category = filterCategory.trim();
      if (filterStatus) params.status = filterStatus;
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;
      if (debouncedSearch.trim()) params.search = debouncedSearch.trim();

      const res = await getTransactions(params);
      const body = res.data;
      const content = body?.content ?? body ?? [];
      setItems(Array.isArray(content) ? content : []);
      setTotalPages(body?.totalPages ?? 1);
      setTotalElements(
        typeof body?.totalElements === "number" ? body.totalElements : content.length
      );
    } catch {
      showError("Failed to load transactions");
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [
    page,
    pageSize,
    filterType,
    filterCategory,
    filterStatus,
    fromDate,
    toDate,
    debouncedSearch,
    showError,
  ]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditing(null);
    setForm({
      ...emptyForm,
      transactionDate: new Date().toISOString().slice(0, 10),
    });
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setEditing(row);
    setForm({
      type: row.type || "EXPENSE",
      amount: row.amount != null ? String(row.amount) : "",
      category: row.category || "",
      description: row.description || "",
      transactionDate: row.transactionDate
        ? String(row.transactionDate).slice(0, 10)
        : new Date().toISOString().slice(0, 10),
    });
    setModalOpen(true);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const amount = Number(form.amount);
    if (!form.category.trim()) {
      showError("Category is required");
      return;
    }
    if (Number.isNaN(amount) || amount <= 0) {
      showError("Enter a valid amount");
      return;
    }
    const payload = {
      type: form.type,
      amount,
      category: form.category.trim(),
      description: form.description.trim() || undefined,
      transactionDate: form.transactionDate || undefined,
    };
    setSaving(true);
    try {
      if (editing?.id != null) {
        await updateTransaction(editing.id, payload);
        success("Transaction updated");
      } else {
        await createTransaction(payload);
        success("Transaction created");
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
    if (!window.confirm("Delete this transaction?")) return;
    try {
      await deleteTransaction(row.id);
      success("Transaction deleted");
      await load();
    } catch {
      showError("Delete failed");
    }
  };

  const handleApprove = async (row) => {
    try {
      await approveTransaction(row.id);
      success("Approved");
      await load();
    } catch {
      showError("Approve failed");
    }
  };

  const handleReject = async (row) => {
    try {
      await rejectTransaction(row.id);
      success("Rejected");
      await load();
    } catch {
      showError("Reject failed");
    }
  };

  const handleExport = async () => {
    try {
      const res = await getTransactions({
        page: 0,
        size: Math.min(totalElements || 500, 500),
        type: filterType || undefined,
        category: filterCategory.trim() || undefined,
        status: filterStatus || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
        search: debouncedSearch.trim() || undefined,
      });
      const body = res.data;
      const rows = body?.content ?? [];
      if (!rows.length) {
        showError("Nothing to export");
        return;
      }
      downloadTransactionsCsv(rows);
      success("CSV downloaded");
    } catch {
      showError("Export failed");
    }
  };

  const columns = [
      {
        key: "transactionDate",
        header: "Date",
        render: (r) => formatDate(r.transactionDate || r.date),
      },
      { key: "type", header: "Type" },
      {
        key: "amount",
        header: "Amount",
        render: (r) => formatCurrency(r.amount),
      },
      { key: "category", header: "Category" },
      {
        key: "status",
        header: "Status",
        render: (r) => (
          <span
            className={`rounded-full px-2 py-0.5 text-xs font-medium ${
              r.status === "APPROVED"
                ? "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-200"
                : r.status === "REJECTED"
                  ? "bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-200"
                  : "bg-amber-100 text-amber-900 dark:bg-amber-900/40 dark:text-amber-100"
            }`}
          >
            {r.status || "—"}
          </span>
        ),
      },
      {
        key: "description",
        header: "Description",
        render: (r) => (
          <span className="max-w-xs truncate block" title={r.description}>
            {r.description || "—"}
          </span>
        ),
      },
      {
        key: "actions",
        header: "",
        render: (r) => (
          <div className="flex flex-wrap gap-2">
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
            {canApprove && (r.status === "PENDING" || !r.status) ? (
              <>
                <button
                  type="button"
                  className="text-xs font-semibold text-emerald-600 hover:underline dark:text-emerald-400"
                  onClick={() => handleApprove(r)}
                >
                  Approve
                </button>
                <button
                  type="button"
                  className="text-xs font-semibold text-amber-700 hover:underline dark:text-amber-300"
                  onClick={() => handleReject(r)}
                >
                  Reject
                </button>
              </>
            ) : null}
          </div>
        ),
      },
    ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Transactions</h1>
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Create, filter, and manage entries. Managers and admins can approve.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={handleExport}
            className="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-800 shadow-sm hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800"
          >
            Export CSV
          </button>
          <button
            type="button"
            onClick={openCreate}
            className="rounded-xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500"
          >
            New transaction
          </button>
        </div>
      </div>

      <div className="grid gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        <FormInput
          label="Search"
          name="search"
          value={searchInput}
          onChange={(e) => {
            setSearchInput(e.target.value);
            setPage(0);
          }}
          placeholder="Description, category…"
        />
        <FormSelect
          label="Type"
          name="filterType"
          value={filterType}
          onChange={(e) => {
            setFilterType(e.target.value);
            setPage(0);
          }}
        >
          <option value="">All</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </FormSelect>
        <FormInput
          label="Category"
          name="filterCategory"
          value={filterCategory}
          onChange={(e) => {
            setFilterCategory(e.target.value);
            setPage(0);
          }}
        />
        <FormSelect
          label="Status"
          name="filterStatus"
          value={filterStatus}
          onChange={(e) => {
            setFilterStatus(e.target.value);
            setPage(0);
          }}
        >
          <option value="">All</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </FormSelect>
        <FormInput
          label="From"
          name="fromDate"
          type="date"
          value={fromDate}
          onChange={(e) => {
            setFromDate(e.target.value);
            setPage(0);
          }}
        />
        <FormInput
          label="To"
          name="toDate"
          type="date"
          value={toDate}
          onChange={(e) => {
            setToDate(e.target.value);
            setPage(0);
          }}
        />
      </div>

      {loading ? (
        <Loader label="Loading transactions" />
      ) : (
        <>
          <Table columns={columns} data={items} emptyMessage="No transactions match your filters." />
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={pageSize}
            onPageChange={(p) => setPage(p)}
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
        title={editing ? "Edit transaction" : "New transaction"}
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
              form="transaction-form"
              disabled={saving}
              className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-60"
            >
              {saving ? "Saving…" : "Save"}
            </button>
          </>
        }
      >
        <form id="transaction-form" className="space-y-4" onSubmit={handleSave}>
          <FormSelect
            label="Type"
            name="type"
            value={form.type}
            onChange={handleFormChange}
            required
          >
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </FormSelect>
          <FormInput
            label="Amount"
            name="amount"
            type="number"
            min="0"
            step="0.01"
            value={form.amount}
            onChange={handleFormChange}
            required
          />
          <FormInput
            label="Category"
            name="category"
            value={form.category}
            onChange={handleFormChange}
            required
          />
          <FormInput
            label="Description"
            name="description"
            value={form.description}
            onChange={handleFormChange}
          />
          <FormInput
            label="Date"
            name="transactionDate"
            type="date"
            value={form.transactionDate}
            onChange={handleFormChange}
            required
          />
        </form>
      </Modal>
    </div>
  );
};

export default TransactionsPage;
