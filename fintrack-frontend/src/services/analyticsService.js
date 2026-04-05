import api from "./api";

export const fetchSummary = () => api.get("/analytics/summary");

export const fetchTrend = () => api.get("/analytics/trend");

export const fetchCategoryBreakdown = () => api.get("/analytics/category");
