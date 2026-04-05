import { useEffect, useState } from "react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import Loader from "../components/Loader";
import { useToast } from "../context/ToastContext";
import {
  fetchSummary,
  fetchTrend,
  fetchCategoryBreakdown,
} from "../services/analyticsService";
import { formatCurrency } from "../utils/format";
import {
  normalizeSummary,
  normalizeTrendData,
  normalizeCategoryData,
} from "../utils/analytics";

const PIE_COLORS = ["#6366f1", "#8b5cf6", "#ec4899", "#f97316", "#14b8a6", "#22c55e"];

const StatCard = ({ title, value, accent }) => (
  <div
    className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900 ${accent}`}
  >
    <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{title}</p>
    <p className="mt-2 text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
      {value}
    </p>
  </div>
);

const DashboardPage = () => {
  const { error: showError } = useToast();
  const [loading, setLoading] = useState(true);
  const [summary, setSummary] = useState(null);
  const [trend, setTrend] = useState([]);
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const [sRes, tRes, cRes] = await Promise.all([
          fetchSummary(),
          fetchTrend(),
          fetchCategoryBreakdown(),
        ]);
        if (cancelled) return;
        setSummary(normalizeSummary(sRes.data));
        setTrend(normalizeTrendData(tRes.data));
        const cats = normalizeCategoryData(cRes.data).filter(
          (x) => !x.type || String(x.type).toUpperCase() === "EXPENSE"
        );
        setCategories(cats.length ? cats : normalizeCategoryData(cRes.data));
      } catch {
        if (!cancelled) showError("Could not load analytics");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => {
      cancelled = true;
    };
  }, [showError]);

  if (loading) {
    return <Loader fullPage label="Loading dashboard" />;
  }

  const expensePie = categories.filter((c) => !c.type || c.type === "EXPENSE");

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Dashboard</h1>
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Income, spending, and category insights.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard
          title="Total income"
          value={formatCurrency(summary?.totalIncome)}
          accent="ring-1 ring-emerald-500/20"
        />
        <StatCard
          title="Total expense"
          value={formatCurrency(summary?.totalExpense)}
          accent="ring-1 ring-rose-500/20"
        />
        <StatCard
          title="Net balance"
          value={formatCurrency(summary?.netBalance)}
          accent="ring-1 ring-indigo-500/20"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900 sm:p-6">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            Monthly trends
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">Income vs expense</p>
          <div className="mt-4 h-72 w-full">
            {trend.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-slate-500 dark:text-slate-400">
                No trend data yet
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={trend}>
                  <CartesianGrid strokeDasharray="3 3" className="stroke-slate-200 dark:stroke-slate-700" />
                  <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                  <YAxis tick={{ fontSize: 11 }} />
                  <Tooltip
                    formatter={(v) => formatCurrency(v)}
                    contentStyle={{ borderRadius: 8 }}
                  />
                  <Legend />
                  <Line type="monotone" dataKey="income" stroke="#22c55e" strokeWidth={2} dot={false} name="Income" />
                  <Line type="monotone" dataKey="expense" stroke="#f43f5e" strokeWidth={2} dot={false} name="Expense" />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900 sm:p-6">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">
            Category breakdown
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Share of totals (expense-focused when type is present)
          </p>
          <div className="mt-4 h-72 w-full">
            {expensePie.length === 0 ? (
              <div className="flex h-full items-center justify-center text-sm text-slate-500 dark:text-slate-400">
                No category data yet
              </div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={expensePie}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={90}
                    paddingAngle={2}
                    label
                  >
                    {expensePie.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v) => formatCurrency(v)} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
