const asList = (raw) => (Array.isArray(raw) ? raw : raw?.data ?? []);

export const normalizeTrendData = (raw) =>
  asList(raw).map((item, i) => ({
    label: item.month ?? item.period ?? item.label ?? `M${i + 1}`,
    income: Number(item.totalIncome ?? item.income ?? 0),
    expense: Number(item.totalExpense ?? item.expense ?? 0),
  }));

export const normalizeCategoryData = (raw) =>
  asList(raw)
    .map((item, idx) => ({
      name: String(item.category ?? item.name ?? `Category ${idx + 1}`),
      value: Math.abs(Number(item.total ?? item.amount ?? item.value ?? 0)),
      type: item.type,
    }))
    .filter((x) => x.value > 0);

export const normalizeSummary = (data) => {
  const income = Number(data?.totalIncome ?? data?.income ?? 0);
  const expense = Number(data?.totalExpense ?? data?.expense ?? 0);
  const net = data?.netBalance ?? data?.balance ?? income - expense;
  return {
    totalIncome: income,
    totalExpense: expense,
    netBalance: Number(net),
  };
};
