import api from "./api";

const withDates = (params) => {
  const p = {};
  if (params?.startDate) p.startDate = params.startDate;
  if (params?.endDate) p.endDate = params.endDate;
  return p;
};

export const fetchSummary = (params) =>
  api.get("/analytics/summary", { params: withDates(params) });

export const fetchTrend = (params) =>
  api.get("/analytics/trend", { params: withDates(params) });

export const fetchCategoryBreakdown = (params) =>
  api.get("/analytics/category", { params: withDates(params) });
