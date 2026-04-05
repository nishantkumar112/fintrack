const escapeCell = (value) => {
  const s = value == null ? "" : String(value);
  if (/[",\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`;
  return s;
};

export const downloadTransactionsCsv = (rows, filename = "transactions.csv") => {
  if (!rows?.length) return;
  const headers = ["id", "type", "amount", "category", "status", "description", "date"];
  const lines = [
    headers.join(","),
    ...rows.map((row) =>
      headers.map((h) => {
        const v = h === "date" ? (row.date ?? row.transactionDate) : row[h];
        return escapeCell(v);
      }).join(",")
    ),
  ];
  const blob = new Blob([lines.join("\n")], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};
