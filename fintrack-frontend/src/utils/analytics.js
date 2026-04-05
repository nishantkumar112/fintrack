export const normalizeTrendData = (raw) => {
  const list = Array.isArray(raw) ? raw : raw?.data ?? raw?.points ?? [];
  return list.map((item, i) => ({
    label:
      item.period ??
      item.month ??
      item.name ??
      item.label ??
      String(item.year && item.month ? `${item.year}-${item.month}` : `P${i + 1}`),
    income: Number(item.income ?? item.totalIncome ?? item.incomeAmount ?? 0),
    expense: Number(item.expense ?? item.totalExpense ?? item.expenseAmount ?? 0),
  }));
};

export const normalizeCategoryData = (raw) => {
  const list = Array.isArray(raw) ? raw : raw?.categories ?? raw?.data ?? [];
  return list
    .map((item, i) => ({
      name: String(item.category ?? item.name ?? `Category ${i + 1}`),
      value: Math.abs(Number(item.amount ?? item.total ?? item.value ?? 0)),
      type: item.type,
    }))
    .filter((x) => x.value > 0);
};

export const normalizeSummary = (data) => {
  const income = Number(data?.totalIncome ?? data?.income ?? 0);
  const expense = Number(data?.totalExpense ?? data?.expense ?? 0);
  const net =
    data?.netBalance ??
    data?.balance ??
    income - expense;
  return {
    totalIncome: income,
    totalExpense: expense,
    netBalance: Number(net),
  };
};
