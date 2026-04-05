import api from "./api";

export const getUsers = (params) => api.get("/users", { params });

export const createUser = (body) => api.post("/users", body);

export const updateUser = (id, body) => api.put(`/users/${id}`, body);

export const deleteUser = (id) => api.delete(`/users/${id}`);
