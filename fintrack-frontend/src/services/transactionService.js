import api from './api';

export const getTransactions = (params) => api.get('/transactions', {params});

export const createTransaction = (body) => api.post('/transactions', body);

export const updateTransaction = (id, body) =>
  api.put(`/transactions/${id}`, body);

export const deleteTransaction = (id) => api.delete(`/transactions/${id}`);

export const approveTransaction = (id) =>
  api.put(`/transactions/${id}/approve`);

export const rejectTransaction = (id) => api.put(`/transactions/${id}/reject`);

export const exportTransaction = (params) =>
  api.get('/transactions/export', {params});
